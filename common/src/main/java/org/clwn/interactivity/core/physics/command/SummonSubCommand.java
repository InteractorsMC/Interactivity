package org.clwn.interactivity.core.physics.command;

import com.github.stephengold.joltjni.Quat;
import com.github.stephengold.joltjni.RVec3;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.clwn.interactivity.api.physics.behavior.SummonableBehavior;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.clwn.interactivity.core.physics.body.ServerBodyManager;
import org.clwn.interactivity.core.physics.body.type.BodyTypeRegistryImpl;

import java.util.Optional;

public final class SummonSubCommand {
    private static final int MAX_COUNT = 256;

    /**
     * Distance between stacked bodies when summoning several at once.
     */
    private static final double STACK_SPACING = 1.5;

    private static final SuggestionProvider<CommandSourceStack> SUMMONABLE_TYPES = (context, builder) ->
            SharedSuggestionProvider.suggestResource(
                    BodyTypeRegistryImpl.INSTANCE.getAll().stream()
                            .filter(type -> type.hasBehavior(SummonableBehavior.class))
                            .map(BodyType::getTypeId),
                    builder
            );

    private SummonSubCommand() {

    }

    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal("summon")
                .then(Commands.argument("type", ResourceLocationArgument.id())
                        .suggests(SUMMONABLE_TYPES)
                        .executes(context -> summon(context, context.getSource().getPosition(), 1))
                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                .executes(context -> summon(context, Vec3Argument.getVec3(context, "pos"), 1))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, MAX_COUNT))
                                        .executes(context -> summon(
                                                context,
                                                Vec3Argument.getVec3(context, "pos"),
                                                IntegerArgumentType.getInteger(context, "count")
                                        ))
                                )
                        )
                );
    }

    private static int summon(CommandContext<CommandSourceStack> context, Vec3 position, int count) {
        CommandSourceStack source = context.getSource();
        ResourceLocation typeId = ResourceLocationArgument.getId(context, "type");

        Optional<BodyType<? extends Body>> type = BodyTypeRegistryImpl.INSTANCE.get(typeId);
        if (type.isEmpty() || !type.get().hasBehavior(SummonableBehavior.class)) {
            source.sendFailure(Component.literal("Unknown or not summonable body type: " + typeId));
            return 0;
        }
        for (int i = 0; i < count; i++) {
            spawn(InteractivityCommand.getPhysicsWorld(source).getBodyManager(), type.get(), position.x, position.y + i * STACK_SPACING, position.z);
        }

        source.sendSuccess(() -> Component.literal("Summoned " + count + " " + typeId), true);
        return count;
    }

    private static <T extends Body> void spawn(ServerBodyManager bodyManager, BodyType<T> type, double x, double y, double z) {
        bodyManager.spawnBody(type, new RVec3(x, y, z), Quat.sIdentity(), body -> {});
    }
}
