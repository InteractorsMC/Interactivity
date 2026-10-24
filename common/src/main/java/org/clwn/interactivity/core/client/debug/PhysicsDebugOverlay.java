/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.client.debug;

import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerLevel;
import org.clwn.interactivity.core.physics.PhysicsPipelineImpl;
import org.clwn.interactivity.core.physics.terrain.TerrainModule;
import org.clwn.interactivity.core.physics.world.SimulatedPhysicsWorldImpl;
import org.clwn.interactivity.core.physics.world.SimulationPauseController;

import java.util.List;
import java.util.Locale;

/**
 * Lines added to the left side of the F3 screen.
 * <p>
 * Server physics is only visible in singleplayer, where the integrated server runs in the same process.
 */
public final class PhysicsDebugOverlay {
    private PhysicsDebugOverlay() {}

    public static void addLines(Minecraft minecraft, List<String> lines) {
        if (minecraft.level == null) {
            return;
        }

        lines.add("");

        IntegratedServer server = minecraft.getSingleplayerServer();
        if (server == null) {
            lines.add("Interactivity: server stats unavailable");
            return;
        }

        ServerLevel serverLevel = server.getLevel(minecraft.level.dimension());
        if (serverLevel == null || !(PhysicsPipelineImpl.INSTANCE.getPhysicsWorld(serverLevel) instanceof SimulatedPhysicsWorldImpl world)) {
            lines.add("Interactivity: no physics world");
            return;
        }

        SimulationPauseController pauseController = world.getPauseController();
        lines.add(String.format(Locale.ROOT, "Interactivity: %s (holds: %d, manual: %s), tick %d (deltaTime: %.1fms)",
                pauseController.isPaused() ? "paused" : "running",
                pauseController.getHoldCount(),
                pauseController.isManuallyPaused(),
                world.getTick(),
                world.getUpdateDeltaTime()
        ));

        TerrainModule terrain = world.getTerrainModule();
        if (terrain == null) {
            lines.add("Terrain: disabled");
            return;
        }

        lines.add(String.format(Locale.ROOT, "Terrain: %d bodies, %d chunks, %d queued, last build %d sections in %.2f ms",
                terrain.getBodyCount(),
                terrain.getLoadedChunkCount(),
                terrain.getQueuedBuilds(),
                terrain.getLastBuildSections(),
                terrain.getLastBuildNanos() / 1_000_000.0
        ));
    }
}
