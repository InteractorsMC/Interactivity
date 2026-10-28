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
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import org.clwn.interactivity.api.physics.behavior.SummonableBehavior;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.clwn.interactivity.core.physics.body.type.BodyTypeRegistryImpl;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Parses a summonable body type with optional summon data, like vanilla {@code /summon}:
 * {@code interactivity:block} or {@code interactivity:block{block_state:"minecraft:oak_log[axis=x]"}}.
 */
public final class BodySpecArgument implements ArgumentType<BodySpec> {
    private static final Collection<String> EXAMPLES = List.of(
            "interactivity:block",
            "interactivity:block{block_state:\"minecraft:oak_log[axis=x]\"}"
    );

    private BodySpecArgument() {
    }

    public static BodySpecArgument bodySpec() {
        return new BodySpecArgument();
    }

    public static BodySpec getBodySpec(CommandContext<?> context, String name) {
        return context.getArgument(name, BodySpec.class);
    }

    @Override
    public BodySpec parse(StringReader reader) throws CommandSyntaxException {
        ResourceLocation type = ResourceLocation.read(reader);

        CompoundTag data = null;
        if (reader.canRead() && reader.peek() == '{') {
            data = new TagParser(reader).readStruct();
        }

        return new BodySpec(type, data);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        // Only the type is suggested, the data is free-form
        if (builder.getRemaining().contains("{")) {
            return builder.buildFuture();
        }

        return SharedSuggestionProvider.suggestResource(
                BodyTypeRegistryImpl.INSTANCE.getAll().stream()
                        .filter(type -> type.hasBehavior(SummonableBehavior.class))
                        .map(BodyType::getTypeId),
                builder
        );
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
