/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.network;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.jetbrains.annotations.Nullable;

/**
 * Maps chunks (packed {@link net.minecraft.world.level.ChunkPos#asLong}) to the network ids of the bodies inside.
 */
public class BodySpatialIndex {
    private final Long2ObjectOpenHashMap<IntOpenHashSet> bodiesByChunk = new Long2ObjectOpenHashMap<>();

    public void add(long chunkKey, int networkId) {
        bodiesByChunk.computeIfAbsent(chunkKey, key -> new IntOpenHashSet()).add(networkId);
    }

    public void remove(long chunkKey, int networkId) {
        IntOpenHashSet bodies = bodiesByChunk.get(chunkKey);
        if (bodies != null && bodies.remove(networkId) && bodies.isEmpty()) {
            bodiesByChunk.remove(chunkKey);
        }
    }

    public void move(int networkId, long oldChunkKey, long newChunkKey) {
        remove(oldChunkKey, networkId);
        add(newChunkKey, networkId);
    }

    /**
     * Returns the chunks that contain at least one body.
     */
    public LongSet occupiedChunks() {
        return bodiesByChunk.keySet();
    }

    /**
     * Returns the network ids of the bodies in a chunk, or {@code null} if there are none.
     */
    public @Nullable IntSet bodiesIn(long chunkKey) {
        return bodiesByChunk.get(chunkKey);
    }
}
