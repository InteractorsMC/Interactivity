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
import org.clwn.interactivity.core.physics.persistence.SaveManager;
import org.clwn.interactivity.core.physics.world.ServerPhysicsWorldImpl;

public final class SaveSubCommand {
    private SaveSubCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal("save")
                .executes(SaveSubCommand::save);
    }

    private static int save(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPhysicsWorldImpl world = InteractivityCommand.getPhysicsWorld(source);
        if (world == null) {
            return 0;
        }

        SaveManager saveManager = world.getSaveManager();
        world.execute(() -> {
            long start = System.nanoTime();
            saveManager.saveAll();

            saveManager.getStorage().flushAll().whenComplete((ignored, error) -> {
                if (error != null) {
                    source.sendFailure(Component.literal("Failed to write physics data: " + error.getMessage()));
                    return;
                }

                long millis = (System.nanoTime() - start) / 1_000_000;
                source.sendSuccess(() -> Component.literal("Save complete in " + millis + " ms"), true);
            });
        });
        return 1;
    }
}
