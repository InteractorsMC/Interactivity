/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.RemovalReason;
import org.clwn.interactivity.core.physics.command.argument.BodySelector;
import org.clwn.interactivity.core.physics.command.argument.BodySelectorArgument;
import org.clwn.interactivity.core.physics.world.ServerPhysicsWorldImpl;

import java.util.List;

public final class KillSubCommand {
    private KillSubCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal("kill")
                .then(Commands.argument("bodies", BodySelectorArgument.bodies())
                        .executes(KillSubCommand::kill)
                );
    }

    private static int kill(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPhysicsWorldImpl world = InteractivityCommand.getPhysicsWorld(source);
        if (world == null) {
            return 0;
        }

        BodySelector selector = BodySelectorArgument.getBodies(context, "bodies");
        Vec3 origin = source.getPosition();

        world.execute(() -> {
            List<Body> bodies = selector.select(world, origin);
            for (Body body : bodies) {
                world.getBodyManager().removeBody(body, RemovalReason.DISCARD);
            }

            if (bodies.isEmpty()) {
                source.sendFailure(Component.literal("No bodies were found"));
            } else {
                source.sendSuccess(() -> Component.literal("Killed " + bodies.size() + " bodies"), true);
            }
        });
        return 1;
    }
}
