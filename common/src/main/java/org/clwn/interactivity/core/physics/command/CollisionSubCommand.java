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
import org.clwn.interactivity.core.physics.command.argument.BodySelector;
import org.clwn.interactivity.core.physics.command.argument.BodySelectorArgument;
import org.clwn.interactivity.core.physics.contact.ContactManagerImpl;
import org.clwn.interactivity.core.physics.world.ServerPhysicsWorldImpl;

import java.util.List;

public final class CollisionSubCommand {
    private CollisionSubCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal("collision")
                .then(Commands.literal("ignore")
                        .then(Commands.argument("bodies", BodySelectorArgument.bodies())
                                .executes(context -> setIgnored(context, true))
                        )
                )
                .then(Commands.literal("unignore")
                        .then(Commands.argument("bodies", BodySelectorArgument.bodies())
                                .executes(context -> setIgnored(context, false))
                        )
                );
    }

    /**
     * Ignores or restores collisions between every pair of the selected bodies.
     */
    private static int setIgnored(CommandContext<CommandSourceStack> context, boolean ignore) {
        CommandSourceStack source = context.getSource();
        ServerPhysicsWorldImpl world = InteractivityCommand.getPhysicsWorld(source);
        if (world == null) {
            return 0;
        }

        BodySelector selector = BodySelectorArgument.getBodies(context, "bodies");
        Vec3 origin = source.getPosition();

        world.execute(() -> {
            List<Body> bodies = selector.select(world, origin);
            if (bodies.size() < 2) {
                source.sendFailure(Component.literal("At least 2 bodies are required"));
                return;
            }

            ContactManagerImpl contactManager = world.getContactManager();
            int pairs = 0;
            for (int i = 0; i < bodies.size(); i++) {
                for (int j = i + 1; j < bodies.size(); j++) {
                    if (ignore) {
                        contactManager.ignoreCollision(bodies.get(i), bodies.get(j));
                    } else {
                        contactManager.unignoreCollision(bodies.get(i), bodies.get(j));
                    }
                    pairs++;
                }
            }

            int count = pairs;
            source.sendSuccess(() -> Component.literal((ignore ? "Ignoring" : "Restored") + " collisions of " + count + " body pairs"), true);
        });
        return 1;
    }
}
