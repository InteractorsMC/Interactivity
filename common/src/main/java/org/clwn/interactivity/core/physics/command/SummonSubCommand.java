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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.clwn.interactivity.api.physics.behavior.SummonableBehavior;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.clwn.interactivity.core.physics.body.ServerBodyManager;
import org.clwn.interactivity.core.physics.body.type.BodyTypeRegistryImpl;
import org.clwn.interactivity.core.physics.command.argument.BodySpec;
import org.clwn.interactivity.core.physics.command.argument.BodySpecArgument;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class SummonSubCommand {
    private static final int MAX_COUNT = 256;

    /**
     * Distance between stacked bodies when summoning several at once.
     */
    private static final double STACK_SPACING = 1.5;

    private SummonSubCommand() {

    }

    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal("summon")
                .then(Commands.argument("body", BodySpecArgument.bodySpec())
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
        BodySpec spec = BodySpecArgument.getBodySpec(context, "body");

        BodyType<? extends Body> type = getSummonableType(source, spec);
        if (type == null) {
            return 0;
        }

        try {
            for (int i = 0; i < count; i++) {
                RVec3 bodyPosition = new RVec3(position.x, position.y + i * STACK_SPACING, position.z);
                InteractivityCommand.getPhysicsWorld(source).getBodyManager().spawnBody(type, bodyPosition, Quat.sIdentity(), body -> configure(body, spec));
            }
        } catch (IllegalArgumentException e) {
            // Thrown by the first body, before anything was spawned
            source.sendFailure(Component.literal("Invalid summon data: " + e.getMessage()));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Summoned " + count + " " + type.getTypeId()), true);
        return count;
    }

    static @Nullable BodyType<? extends Body> getSummonableType(CommandSourceStack source, BodySpec spec) {
        Optional<BodyType<? extends Body>> type = BodyTypeRegistryImpl.INSTANCE.get(spec.type());
        Optional<SummonableBehavior> summonable = type.flatMap(bodyType -> bodyType.getBehavior(SummonableBehavior.class));
        if (summonable.isEmpty()) {
            source.sendFailure(Component.literal("Unknown or not summonable body type: " + spec.type()));
            return null;
        }

        if (spec.data() != null && summonable.get().configurator() == null) {
            source.sendFailure(Component.literal("Body type " + spec.type() + " does not accept summon data"));
            return null;
        }

        return type.get();
    }

    static void configure(Body body, BodySpec spec) {
        CompoundTag data = spec.data();
        if (data == null) {
            return;
        }

        body.getType().getBehavior(SummonableBehavior.class)
                .map(SummonableBehavior::configurator)
                .ifPresent(configurator -> configurator.configure(body, data.copy()));
    }
}
