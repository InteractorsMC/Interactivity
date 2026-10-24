/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.terrain;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Copy of the block data of a chunk section, taken on the thread that owns the level, so it can be read on the physics thread.
 *
 * @param sectionKey {@link SectionPos#asLong} of the section
 * @param states     copy of the block states, null when the section is empty
 * @param overrides  collision shapes by cell index ({@code y << 8 | z << 4 | x}) for blocks whose shape depends on
 *                   their position or block entity, null when there are none
 */
public record SectionSnapshot(
        long sectionKey,
        @Nullable PalettedContainer<BlockState> states,
        @Nullable Int2ObjectMap<VoxelShape> overrides
) {
    private static final Predicate<BlockState> HAS_DYNAMIC_SHAPE = state -> state.getBlock().hasDynamicShape();

    /**
     * Must be called on the thread that owns the level of the chunk.
     */
    public static SectionSnapshot of(LevelChunk chunk, int sectionIndex) {
        Level level = chunk.getLevel();
        LevelChunkSection section = chunk.getSection(sectionIndex);
        int sectionY = level.getSectionYFromSectionIndex(sectionIndex);
        long sectionKey = SectionPos.asLong(chunk.getPos().x, sectionY, chunk.getPos().z);

        if (section.hasOnlyAir()) {
            return new SectionSnapshot(sectionKey, null, null);
        }

        // States without a cached collision shape need the level, which is only readable here
        Int2ObjectMap<VoxelShape> overrides = null;
        if (section.maybeHas(HAS_DYNAMIC_SHAPE)) {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            int baseX = SectionPos.sectionToBlockCoord(chunk.getPos().x);
            int baseY = SectionPos.sectionToBlockCoord(sectionY);
            int baseZ = SectionPos.sectionToBlockCoord(chunk.getPos().z);

            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        BlockState state = section.getBlockState(x, y, z);
                        if (HAS_DYNAMIC_SHAPE.test(state)) {
                            if (overrides == null) {
                                overrides = new Int2ObjectOpenHashMap<>();
                            }
                            overrides.put(cellIndex(x, y, z), state.getCollisionShape(level, pos.set(baseX + x, baseY + y, baseZ + z)));
                        }
                    }
                }
            }
        }

        return new SectionSnapshot(sectionKey, section.getStates().copy(), overrides);
    }

    public static int cellIndex(int x, int y, int z) {
        return (y << 8) | (z << 4) | x;
    }
}
