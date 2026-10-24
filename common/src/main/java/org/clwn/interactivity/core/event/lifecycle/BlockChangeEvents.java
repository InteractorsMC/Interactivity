/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.event.lifecycle;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.clwn.interactivity.core.event.Event;

public class BlockChangeEvents {
    /**
     * A block state in a {@link LevelChunk} was replaced with a different one.
     * <p>
     * Published on the thread that owns the level, on both logical sides, for every change made through
     * {@link LevelChunk#setBlockState}. World generation writes to proto chunks are not included.
     */
    public static final Event<BlockChangeEvent> BLOCK_CHANGED_EVENT = new Event<>();

    /**
     * @param pos may be a mutable position reused by the caller, so it must not be stored
     */
    public record BlockChangeEvent(Level level, LevelChunk chunk, BlockPos pos, BlockState oldState, BlockState newState) {
    }
}
