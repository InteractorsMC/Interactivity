/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.clwn.interactivity.core.physics.body.type.BodyTypeRegistryImpl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Parses {@code @b[type=<id>,distance=<range>,limit=<n>,sort=<order>]} or a body UUID.
 * <p>
 * {@code type} can be negated with {@code !}, {@code sort} is one of {@code nearest}, {@code furthest},
 * {@code random} and {@code arbitrary} (default).
 */
public final class BodySelectorArgument implements ArgumentType<BodySelector> {
    private static final Collection<String> EXAMPLES = List.of(
            "@b",
            "@b[type=interactivity:block,distance=..10]",
            "@b[sort=nearest,limit=1]",
            "dd12be42-52a9-4a91-a8a1-11c01849e498"
    );

    private static final List<String> OPTIONS = List.of("type=", "distance=", "limit=", "sort=");

    private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(Component.literal("Expected @b or a body UUID"));
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_OPTION = new DynamicCommandExceptionType(option -> Component.literal("Unknown option '" + option + "'"));
    private static final SimpleCommandExceptionType ERROR_EXPECTED_VALUE = new SimpleCommandExceptionType(Component.literal("Expected '=' and a value"));
    private static final SimpleCommandExceptionType ERROR_EXPECTED_END = new SimpleCommandExceptionType(Component.literal("Expected ',' or ']'"));
    private static final SimpleCommandExceptionType ERROR_NEGATIVE_DISTANCE = new SimpleCommandExceptionType(Component.literal("Distance cannot be negative"));
    private static final SimpleCommandExceptionType ERROR_LIMIT_TOO_SMALL = new SimpleCommandExceptionType(Component.literal("Limit must be at least 1"));
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_SORT = new DynamicCommandExceptionType(sort -> Component.literal("Unknown sort '" + sort + "'"));

    private BodySelectorArgument() {
    }

    public static BodySelectorArgument bodies() {
        return new BodySelectorArgument();
    }

    public static BodySelector getBodies(CommandContext<?> context, String name) {
        return context.getArgument(name, BodySelector.class);
    }

    @Override
    public BodySelector parse(StringReader reader) throws CommandSyntaxException {
        return new Parser(reader).parse();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        StringReader reader = new StringReader(builder.getInput());
        reader.setCursor(builder.getStart());

        Parser parser = new Parser(reader);
        try {
            parser.parse();
        } catch (CommandSyntaxException ignored) {
        }
        return parser.suggestions.apply(builder.createOffset(parser.suggestionStart));
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    private static Stream<String> typeIds() {
        return BodyTypeRegistryImpl.INSTANCE.getAll().stream().map(BodyType::getTypeId).map(ResourceLocation::toString);
    }

    private static final class Parser {
        private final StringReader reader;

        private int suggestionStart;
        private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions = SuggestionsBuilder::buildFuture;

        private ResourceLocation type;
        private boolean typeInverted;
        private MinMaxBounds.Doubles distance = MinMaxBounds.Doubles.ANY;
        private int limit = Integer.MAX_VALUE;
        private BodySelector.Sort sort = BodySelector.Sort.ARBITRARY;

        private Parser(StringReader reader) {
            this.reader = reader;
        }

        private BodySelector parse() throws CommandSyntaxException {
            int start = reader.getCursor();
            suggest(builder -> SharedSuggestionProvider.suggest(List.of("@b"), builder));

            if (reader.canRead() && reader.peek() == '@') {
                reader.skip();
                if (!reader.canRead() || reader.peek() != 'b') {
                    reader.setCursor(start);
                    throw ERROR_INVALID.createWithContext(reader);
                }
                reader.skip();

                suggest(builder -> SharedSuggestionProvider.suggest(List.of("["), builder));
                if (reader.canRead() && reader.peek() == '[') {
                    reader.skip();
                    parseOptions();
                }
                return new BodySelector(null, type, typeInverted, distance, limit, sort);
            }

            while (reader.canRead() && reader.peek() != ' ') {
                reader.skip();
            }
            try {
                UUID uuid = UUID.fromString(reader.getString().substring(start, reader.getCursor()));
                return new BodySelector(uuid, null, false, MinMaxBounds.Doubles.ANY, 1, BodySelector.Sort.ARBITRARY);
            } catch (IllegalArgumentException e) {
                reader.setCursor(start);
                throw ERROR_INVALID.createWithContext(reader);
            }
        }

        private void parseOptions() throws CommandSyntaxException {
            while (true) {
                reader.skipWhitespace();
                suggest(builder -> SharedSuggestionProvider.suggest(Stream.concat(OPTIONS.stream(), Stream.of("]")), builder));
                if (reader.canRead() && reader.peek() == ']') {
                    reader.skip();
                    suggest(SuggestionsBuilder::buildFuture);
                    return;
                }

                int keyStart = reader.getCursor();
                String key = reader.readUnquotedString();
                reader.skipWhitespace();
                if (!reader.canRead() || reader.peek() != '=') {
                    reader.setCursor(keyStart);
                    throw ERROR_EXPECTED_VALUE.createWithContext(reader);
                }
                reader.skip();
                reader.skipWhitespace();

                boolean valueSuggested = parseValue(key, keyStart);

                reader.skipWhitespace();
                if (reader.canRead() || !valueSuggested) {
                    suggest(builder -> SharedSuggestionProvider.suggest(List.of(",", "]"), builder));
                }

                if (reader.canRead() && reader.peek() == ',') {
                    reader.skip();
                } else if (reader.canRead() && reader.peek() == ']') {
                    reader.skip();
                    suggest(SuggestionsBuilder::buildFuture);
                    return;
                } else {
                    throw ERROR_EXPECTED_END.createWithContext(reader);
                }
            }
        }

        /**
         * @return {@code true} if the value has its own suggestions, which are kept while it is the last token
         */
        private boolean parseValue(String key, int keyStart) throws CommandSyntaxException {
            int valueStart = reader.getCursor();
            switch (key) {
                case "type" -> {
                    suggest(builder -> SharedSuggestionProvider.suggest(
                            typeIds().flatMap(id -> Stream.of(id, "!" + id)), builder
                    ));
                    typeInverted = reader.canRead() && reader.peek() == '!';
                    if (typeInverted) {
                        reader.skip();
                        suggest(builder -> SharedSuggestionProvider.suggest(typeIds(), builder));
                    }
                    type = ResourceLocation.read(reader);
                    return true;
                }
                case "distance" -> {
                    suggest(SuggestionsBuilder::buildFuture);
                    distance = MinMaxBounds.Doubles.fromReader(reader);
                    if (distance.min().filter(min -> min < 0).isPresent() || distance.max().filter(max -> max < 0).isPresent()) {
                        reader.setCursor(valueStart);
                        throw ERROR_NEGATIVE_DISTANCE.createWithContext(reader);
                    }
                    return false;
                }
                case "limit" -> {
                    suggest(SuggestionsBuilder::buildFuture);
                    limit = reader.readInt();
                    if (limit < 1) {
                        reader.setCursor(valueStart);
                        throw ERROR_LIMIT_TOO_SMALL.createWithContext(reader);
                    }
                    return false;
                }
                case "sort" -> {
                    suggest(builder -> SharedSuggestionProvider.suggest(
                            Arrays.stream(BodySelector.Sort.values()).map(BodySelector.Sort::getName), builder
                    ));
                    String name = reader.readUnquotedString();
                    sort = BodySelector.Sort.byName(name);
                    if (sort == null) {
                        reader.setCursor(valueStart);
                        throw ERROR_UNKNOWN_SORT.createWithContext(reader, name);
                    }
                    return true;
                }
                default -> {
                    reader.setCursor(keyStart);
                    throw ERROR_UNKNOWN_OPTION.createWithContext(reader, key);
                }
            }
        }

        private void suggest(Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions) {
            this.suggestionStart = reader.getCursor();
            this.suggestions = suggestions;
        }
    }
}
