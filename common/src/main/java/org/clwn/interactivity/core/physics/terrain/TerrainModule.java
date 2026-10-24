/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.terrain;

import com.github.stephengold.joltjni.Jolt;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.api.physics.ObjectLayer;
import org.clwn.interactivity.core.config.InteractivityConfig;
import org.clwn.interactivity.core.physics.world.SimulatedPhysicsWorldImpl;
import org.clwn.interactivity.core.physics.world.SimulationPauseController;
import org.clwn.interactivity.natives.TerrainNative;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Builds static collision bodies out of the blocks of loaded chunks, one body per non-empty chunk section.
 * <p>
 * The level thread only copies section data ({@link SectionSnapshot}) and queues a command; boxes and bodies are built
 * on the physics thread. The simulation is paused from the moment a chunk loads until its bodies exist,
 * so bodies never fall through terrain that is not built yet. Rebuilds after block changes are collected during a level tick
 * and only pause the simulation when {@link InteractivityConfig#terrainPauseOnBlockUpdates} is set.
 * <p>
 * Because every command goes through the same FIFO queue, a chunk is always built before its rebuilds and removed after them.
 */
public class TerrainModule {
    private final SimulatedPhysicsWorldImpl world;
    private final Level level;

    private volatile boolean closed;

    // Level thread state
    private final LongSet loadedChunks = new LongOpenHashSet();
    private final LongSet dirtySections = new LongOpenHashSet();

    // Physics thread state
    private final GreedyBoxMesher mesher;
    private final Long2IntOpenHashMap bodyBySection = new Long2IntOpenHashMap();
    private final Int2LongOpenHashMap sectionByBody = new Int2LongOpenHashMap();
    private final Batch batch = new Batch();

    // Statistics
    private final AtomicInteger queuedBuilds = new AtomicInteger();
    @Getter
    private volatile int loadedChunkCount;
    @Getter
    private volatile int bodyCount;
    @Getter
    private volatile long lastBuildNanos;
    @Getter
    private volatile int lastBuildSections;

    public TerrainModule(SimulatedPhysicsWorldImpl world, TerrainBlockClassifier classifier) {
        this.world = world;
        this.level = world.getLevel();
        this.mesher = new GreedyBoxMesher(classifier);
        this.bodyBySection.defaultReturnValue(Jolt.cInvalidBodyId);
    }

    public int getQueuedBuilds() {
        return queuedBuilds.get();
    }

    /**
     * Returns the {@link SectionPos#asLong} of the section a terrain body belongs to. Physics thread only.
     *
     * @return the section, or {@link Long#MIN_VALUE} if the body is not a terrain body of this world
     */
    public long getSectionOfBody(int bodyId) {
        return sectionByBody.getOrDefault(bodyId, Long.MIN_VALUE);
    }

    public void onChunkLoad(LevelChunk chunk) {
        if (closed) {
            return;
        }

        loadedChunks.add(chunk.getPos().toLong());
        loadedChunkCount = loadedChunks.size();

        List<SectionSnapshot> snapshots = new ArrayList<>();
        for (int i = 0; i < chunk.getSectionsCount(); i++) {
            if (!chunk.getSection(i).hasOnlyAir()) {
                snapshots.add(SectionSnapshot.of(chunk, i));
            }
        }

        if (!snapshots.isEmpty()) {
            queueBuild(snapshots, world.acquireSimulationPause("terrain of chunk " + chunk.getPos()));
        }
    }

    public void onChunkUnload(LevelChunk chunk) {
        if (closed || !loadedChunks.remove(chunk.getPos().toLong())) {
            return;
        }
        loadedChunkCount = loadedChunks.size();

        queueRemoval(chunk.getPos());
    }

    public void onBlockChanged(LevelChunk chunk, BlockPos pos, BlockState oldState, BlockState newState) {
        if (closed || !loadedChunks.contains(chunk.getPos().toLong())) {
            return;
        }

        // States with equal collision usually share the shape instance
        if (oldState.getCollisionShape(chunk, pos) == newState.getCollisionShape(chunk, pos)) {
            return;
        }

        dirtySections.add(SectionPos.asLong(
                SectionPos.blockToSectionCoord(pos.getX()),
                SectionPos.blockToSectionCoord(pos.getY()),
                SectionPos.blockToSectionCoord(pos.getZ())
        ));
    }

    public void onLevelTickEnd() {
        if (closed || dirtySections.isEmpty()) {
            return;
        }

        List<SectionSnapshot> snapshots = new ArrayList<>(dirtySections.size());
        LongIterator iterator = dirtySections.iterator();
        while (iterator.hasNext()) {
            long sectionKey = iterator.nextLong();
            int chunkX = SectionPos.x(sectionKey);
            int chunkZ = SectionPos.z(sectionKey);

            if (!loadedChunks.contains(ChunkPos.asLong(chunkX, chunkZ))) {
                continue;
            }

            LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
            int sectionIndex = level.getSectionIndexFromSectionY(SectionPos.y(sectionKey));
            if (chunk != null && sectionIndex >= 0 && sectionIndex < chunk.getSectionsCount()) {
                snapshots.add(SectionSnapshot.of(chunk, sectionIndex));
            }
        }
        dirtySections.clear();

        if (!snapshots.isEmpty()) {
            SimulationPauseController.Hold hold = InteractivityConfig.terrainPauseOnBlockUpdates
                    ? world.acquireSimulationPause("terrain rebuild")
                    : null;
            queueBuild(snapshots, hold);
        }
    }

    /**
     * Bodies are not removed here, they are destroyed together with the physics system.
     */
    public void close() {
        closed = true;
    }

    private void queueBuild(List<SectionSnapshot> snapshots, @Nullable SimulationPauseController.Hold hold) {
        queuedBuilds.incrementAndGet();
        world.execute(() -> {
            try {
                build(snapshots);
            } finally {
                queuedBuilds.decrementAndGet();
                if (hold != null) {
                    hold.release();
                }
            }
        });
    }

    private void queueRemoval(ChunkPos chunkPos) {
        int minSection = level.getMinSection();
        int maxSection = level.getMaxSection();
        world.execute(() -> removeChunk(chunkPos.x, chunkPos.z, minSection, maxSection));
    }

    /**
     * Creates, reshapes or removes the body of every snapshot section.
     */
    private void build(List<SectionSnapshot> snapshots) {
        long start = System.nanoTime();
        long physicsSystemVa = world.getJoltBridge().getPhysicsSystem().va();

        mesher.reset();
        batch.prepare(snapshots.size());

        for (SectionSnapshot snapshot : snapshots) {
            long sectionKey = snapshot.sectionKey();
            int boxStart = mesher.getBoxCount();
            int boxCount = mesher.append(snapshot);
            int bodyId = bodyBySection.get(sectionKey);

            if (boxCount == 0) {
                if (bodyId != Jolt.cInvalidBodyId) {
                    forgetBody(sectionKey, bodyId);
                    batch.removeIds.put(batch.removeCount++, bodyId);
                }
            } else if (bodyId != Jolt.cInvalidBodyId) {
                if (!TerrainNative.setTerrainShape(physicsSystemVa, bodyId, mesher.getBoxes(), mesher.getTags(), boxStart, boxCount)) {
                    Interactivity.LOGGER.warn("Failed to rebuild terrain of section {}", SectionPos.of(sectionKey));
                }
            } else {
                batch.addCreate(sectionKey, boxStart, boxCount);
            }
        }

        if (batch.removeCount > 0) {
            TerrainNative.removeTerrainBodies(physicsSystemVa, batch.removeIds, batch.removeCount, true);
        }

        if (batch.createCount > 0) {
            TerrainNative.createTerrainBodies(
                    physicsSystemVa,
                    ObjectLayer.TERRAIN.getValue(),
                    batch.createCount,
                    batch.origins,
                    batch.boxStarts,
                    batch.boxCounts,
                    mesher.getBoxes(),
                    mesher.getTags(),
                    batch.createdIds
            );

            for (int i = 0; i < batch.createCount; i++) {
                int bodyId = batch.createdIds.get(i);
                long sectionKey = batch.createKeys[i];
                if (bodyId == Jolt.cInvalidBodyId) {
                    Interactivity.LOGGER.warn("Failed to create terrain of section {}", SectionPos.of(sectionKey));
                    continue;
                }
                bodyBySection.put(sectionKey, bodyId);
                sectionByBody.put(bodyId, sectionKey);
            }
        }

        bodyCount = bodyBySection.size();
        lastBuildSections = snapshots.size();
        lastBuildNanos = System.nanoTime() - start;

        Interactivity.LOGGER.debug("Built terrain of {} sections ({} boxes) in {} µs", snapshots.size(), mesher.getBoxCount(), lastBuildNanos / 1_000);
    }

    /**
     * Removes the bodies of a chunk without waking bodies around them. Physics thread only.
     * <p>
     * Bodies inside unloaded chunks are left alone, keeping them is up to whoever manages them (e.g. a save manager).
     */
    private void removeChunk(int chunkX, int chunkZ, int minSection, int maxSection) {
        batch.prepare(maxSection - minSection);

        for (int sectionY = minSection; sectionY < maxSection; sectionY++) {
            long sectionKey = SectionPos.asLong(chunkX, sectionY, chunkZ);
            int bodyId = bodyBySection.get(sectionKey);
            if (bodyId != Jolt.cInvalidBodyId) {
                forgetBody(sectionKey, bodyId);
                batch.removeIds.put(batch.removeCount++, bodyId);
            }
        }

        if (batch.removeCount > 0) {
            TerrainNative.removeTerrainBodies(world.getJoltBridge().getPhysicsSystem().va(), batch.removeIds, batch.removeCount, false);
        }
        bodyCount = bodyBySection.size();
    }

    private void forgetBody(long sectionKey, int bodyId) {
        bodyBySection.remove(sectionKey);
        sectionByBody.remove(bodyId);
    }

    /**
     * Reusable native buffers for one {@link #build} or {@link #removeChunk} call.
     */
    private static final class Batch {
        private int capacity;

        private long[] createKeys;
        private DoubleBuffer origins;
        private IntBuffer boxStarts;
        private IntBuffer boxCounts;
        private IntBuffer createdIds;
        private IntBuffer removeIds;

        private int createCount;
        private int removeCount;

        private Batch() {
            allocate(64);
        }

        private void prepare(int sectionCount) {
            if (sectionCount > capacity) {
                allocate(Math.max(sectionCount, capacity * 2));
            }
            createCount = 0;
            removeCount = 0;
        }

        private void addCreate(long sectionKey, int boxStart, int boxCount) {
            int i = createCount++;
            createKeys[i] = sectionKey;
            origins.put(i * 3, SectionPos.sectionToBlockCoord(SectionPos.x(sectionKey)));
            origins.put(i * 3 + 1, SectionPos.sectionToBlockCoord(SectionPos.y(sectionKey)));
            origins.put(i * 3 + 2, SectionPos.sectionToBlockCoord(SectionPos.z(sectionKey)));
            boxStarts.put(i, boxStart);
            boxCounts.put(i, boxCount);
        }

        private void allocate(int newCapacity) {
            capacity = newCapacity;
            createKeys = new long[newCapacity];
            origins = ByteBuffer.allocateDirect(newCapacity * 3 * Double.BYTES).order(ByteOrder.nativeOrder()).asDoubleBuffer();
            boxStarts = allocateInts(newCapacity);
            boxCounts = allocateInts(newCapacity);
            createdIds = allocateInts(newCapacity);
            removeIds = allocateInts(newCapacity);
        }

        private static IntBuffer allocateInts(int count) {
            return ByteBuffer.allocateDirect(count * Integer.BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();
        }
    }
}
