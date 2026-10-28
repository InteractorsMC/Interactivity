/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;
import org.clwn.interactivity.core.physics.PhysicsPipelineImpl;
import org.clwn.interactivity.core.physics.world.ServerPhysicsWorldImpl;
import org.jetbrains.annotations.Nullable;

public final class InteractivityCommand {
    private InteractivityCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("interactivity")
                .requires(source -> source.hasPermission(2))
                .then(SummonSubCommand.getCommand())
                .then(KillSubCommand.getCommand())
                .then(ConstraintSubCommand.getCommand())
                .then(SaveSubCommand.getCommand())
        );
    }

    static @Nullable ServerPhysicsWorldImpl getPhysicsWorld(CommandSourceStack source) {
        PhysicsWorld world = PhysicsPipelineImpl.INSTANCE == null ? null : PhysicsPipelineImpl.INSTANCE.getPhysicsWorld(source.getLevel());
        if (!(world instanceof ServerPhysicsWorldImpl serverWorld)) {
            source.sendFailure(Component.literal("There is no physics world in this dimension"));
            return null;
        }
        return serverWorld;
    }
}
