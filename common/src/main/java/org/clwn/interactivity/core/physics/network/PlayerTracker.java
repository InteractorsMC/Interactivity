/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.network;

import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.server.level.ServerPlayer;

public class PlayerTracker {
    public final ServerPlayer player;

    /**
     * Chunks the player receives, as last seen by the dispatcher.
     */
    public ChunkTrackingView view = ChunkTrackingView.EMPTY;

    /**
     * Network ids of the bodies the client has, or will have once the queued spawns are sent.
     */
    public final IntOpenHashSet tracked = new IntOpenHashSet();

    public final IntLinkedOpenHashSet pendingSpawn = new IntLinkedOpenHashSet();
    public final IntLinkedOpenHashSet pendingRemove = new IntLinkedOpenHashSet();

    public final IntOpenHashSet spawnedThisTick = new IntOpenHashSet();

    public PlayerTracker(ServerPlayer player) {
        this.player = player;
    }

    public void track(int networkId) {
        if (!tracked.add(networkId)) {
            return;
        }

        if (pendingRemove.remove(networkId)) {
            return;
        }

        pendingSpawn.add(networkId);
    }

    public void untrack(int networkId) {
        if (!tracked.remove(networkId)) {
            return;
        }

        if (pendingSpawn.remove(networkId)) {
            return;
        }

        pendingRemove.add(networkId);
    }
}
