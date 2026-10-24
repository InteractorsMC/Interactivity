/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.terrain;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.clwn.interactivity.core.util.buffer.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public final class GreedyBoxMesher {
    private static final int SIZE = 16;
    private static final int CELLS = SIZE * SIZE * SIZE;
    private static final int FLOATS_PER_BOX = 6;

    private final TerrainBlockClassifier classifier;

    private final boolean[] full = new boolean[CELLS];
    private final int[] fullTags = new int[CELLS];

    @Getter
    private FloatBuffer boxes;
    @Getter
    private IntBuffer tags;
    @Getter
    private int boxCount;
    private int capacity;

    public GreedyBoxMesher(TerrainBlockClassifier classifier) {
        this.classifier = classifier;
        allocate(1024);
    }

    public void reset() {
        boxCount = 0;
    }

    /**
     * Appends the boxes of a section.
     *
     * @return number of appended boxes
     */
    public int append(SectionSnapshot snapshot) {
        PalettedContainer<BlockState> states = snapshot.states();
        if (states == null) {
            return 0;
        }

        int start = boxCount;
        Int2ObjectMap<VoxelShape> overrides = snapshot.overrides();
        boolean anyFull = false;

        for (int y = 0; y < SIZE; y++) {
            for (int z = 0; z < SIZE; z++) {
                for (int x = 0; x < SIZE; x++) {
                    int index = SectionSnapshot.cellIndex(x, y, z);
                    full[index] = false;

                    BlockState state = states.get(x, y, z);
                    VoxelShape override = overrides != null ? overrides.get(index) : null;

                    if (override != null) {
                        appendShape(override, x, y, z, classifier.tag(state));
                    } else if (state.isAir()) {
                        // fast path for the most common block
                    } else if (state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) {
                        full[index] = true;
                        fullTags[index] = classifier.tag(state);
                        anyFull = true;
                    } else {
                        appendShape(state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO), x, y, z, classifier.tag(state));
                    }
                }
            }
        }

        if (anyFull) {
            mergeFullBlocks();
        }

        return boxCount - start;
    }

    private void mergeFullBlocks() {
        for (int y = 0; y < SIZE; y++) {
            for (int z = 0; z < SIZE; z++) {
                for (int x = 0; x < SIZE; x++) {
                    int index = SectionSnapshot.cellIndex(x, y, z);
                    if (!full[index]) {
                        continue;
                    }
                    int tag = fullTags[index];

                    int maxX = x + 1;
                    while (maxX < SIZE && matches(maxX, y, z, tag)) {
                        maxX++;
                    }

                    int maxZ = z + 1;
                    while (maxZ < SIZE && rowMatches(x, maxX, y, maxZ, tag)) {
                        maxZ++;
                    }

                    int maxY = y + 1;
                    while (maxY < SIZE && layerMatches(x, maxX, maxY, z, maxZ, tag)) {
                        maxY++;
                    }

                    for (int cy = y; cy < maxY; cy++) {
                        for (int cz = z; cz < maxZ; cz++) {
                            for (int cx = x; cx < maxX; cx++) {
                                full[SectionSnapshot.cellIndex(cx, cy, cz)] = false;
                            }
                        }
                    }

                    appendBox(x, y, z, maxX, maxY, maxZ, tag);
                }
            }
        }
    }

    private boolean matches(int x, int y, int z, int tag) {
        int index = SectionSnapshot.cellIndex(x, y, z);
        return full[index] && fullTags[index] == tag;
    }

    private boolean rowMatches(int minX, int maxX, int y, int z, int tag) {
        for (int x = minX; x < maxX; x++) {
            if (!matches(x, y, z, tag)) {
                return false;
            }
        }
        return true;
    }

    private boolean layerMatches(int minX, int maxX, int y, int minZ, int maxZ, int tag) {
        for (int z = minZ; z < maxZ; z++) {
            if (!rowMatches(minX, maxX, y, z, tag)) {
                return false;
            }
        }
        return true;
    }

    private void appendShape(VoxelShape shape, int x, int y, int z, int tag) {
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                appendBox(x + minX, y + minY, z + minZ, x + maxX, y + maxY, z + maxZ, tag));
    }

    private void appendBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int tag) {
        if (boxCount == capacity) {
            allocate(capacity * 2);
        }

        int offset = boxCount * FLOATS_PER_BOX;
        boxes.put(offset, (float) minX);
        boxes.put(offset + 1, (float) minY);
        boxes.put(offset + 2, (float) minZ);
        boxes.put(offset + 3, (float) maxX);
        boxes.put(offset + 4, (float) maxY);
        boxes.put(offset + 5, (float) maxZ);
        tags.put(boxCount, tag);
        boxCount++;
    }

    private void allocate(int newCapacity) {
        FloatBuffer newBoxes = ByteBuffer.allocateDirect(newCapacity * FLOATS_PER_BOX * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        IntBuffer newTags = ByteBuffer.allocateDirect(newCapacity * Integer.BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();

        if (boxes != null) {
            BufferUtils.copyFloatBuffer(boxes, 0, newBoxes, 0, boxCount * FLOATS_PER_BOX);
            BufferUtils.copyIntBuffer(tags, 0, newTags, 0, boxCount);
        }

        boxes = newBoxes;
        tags = newTags;
        capacity = newCapacity;
    }
}
