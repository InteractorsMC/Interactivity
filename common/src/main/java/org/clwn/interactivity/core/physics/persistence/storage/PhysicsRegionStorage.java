/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.persistence.storage;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.world.level.ChunkPos;
import org.clwn.interactivity.Interactivity;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.*;

/**
 * Chunk payloads of one dimension, kept in {@link PhysicsRegionFile}s.
 * <p>
 * All file access and the region cache live on one IO thread. Writes only change the cache and are flushed to disk
 * a second later, so several writes to a region turn into one file write.
 */
public class PhysicsRegionStorage {
    private static final long FLUSH_DELAY_MILLIS = 1_000;
    private static final long EVICT_INTERVAL_MILLIS = 30_000;
    private static final long IDLE_MILLIS = 60_000;
    private static final long CLOSE_TIMEOUT_SECONDS = 30;

    private final Path folder;
    private final ScheduledExecutorService executor;

    // IO thread state
    private final Long2ObjectOpenHashMap<Region> regions = new Long2ObjectOpenHashMap<>();
    private @Nullable ScheduledFuture<?> scheduledFlush;

    public PhysicsRegionStorage(Path folder, String threadName) {
        this.folder = folder;
        this.executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, threadName);
            thread.setDaemon(true);
            return thread;
        });
        this.executor.scheduleWithFixedDelay(this::evictIdle, EVICT_INTERVAL_MILLIS, EVICT_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
    }

    /**
     * Reads the payload of a chunk, including writes that are not flushed yet.
     *
     * @return the payload, or {@code null} if the chunk has none
     */
    public CompletableFuture<byte[]> read(long chunkKey) {
        return CompletableFuture.supplyAsync(() -> getRegion(chunkKey).chunks[localIndex(chunkKey)], executor);
    }

    /**
     * Replaces the payload of a chunk.
     *
     * @param payload the new payload, or {@code null} to remove it
     */
    public void write(long chunkKey, byte[] payload) {
        try {
            executor.execute(() -> {
                Region region = getRegion(chunkKey);
                region.chunks[localIndex(chunkKey)] = payload;
                region.dirty = true;
                scheduleFlush();
            });
        } catch (RejectedExecutionException e) {
            Interactivity.LOGGER.error("Physics data of chunk [{}, {}] was not saved, storage {} is closed", ChunkPos.getX(chunkKey), ChunkPos.getZ(chunkKey), folder);
        }
    }

    /**
     * Writes all changed regions to disk.
     */
    public CompletableFuture<Void> flushAll() {
        return CompletableFuture.runAsync(this::flush, executor);
    }

    /**
     * Flushes everything and stops the IO thread. Blocks until done.
     */
    public void close() {
        try {
            flushAll().get(CLOSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            Interactivity.LOGGER.error("Failed to flush physics storage {}", folder, e);
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(CLOSE_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                Interactivity.LOGGER.error("Physics storage {} did not stop in time", folder);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Region getRegion(long chunkKey) {
        int regionX = ChunkPos.getX(chunkKey) >> 5;
        int regionZ = ChunkPos.getZ(chunkKey) >> 5;
        long regionKey = ChunkPos.asLong(regionX, regionZ);

        Region region = regions.get(regionKey);
        if (region == null) {
            region = new Region(folder.resolve("r." + regionX + "." + regionZ + ".imcphys"));
            region.chunks = load(region.path);
            regions.put(regionKey, region);
        }

        region.lastAccess = System.currentTimeMillis();
        return region;
    }

    private static byte[][] load(Path path) {
        try {
            return PhysicsRegionFile.read(path);
        } catch (IOException | RuntimeException e) {
            Path corrupt = path.resolveSibling(path.getFileName() + ".corrupt-" + System.currentTimeMillis() / 1000);
            Interactivity.LOGGER.error("Physics region {} is corrupt, moving it to {} and starting empty", path, corrupt.getFileName(), e);
            try {
                Files.move(path, corrupt);
            } catch (IOException moveError) {
                Interactivity.LOGGER.error("Failed to move corrupt physics region {}", path, moveError);
            }
            return new byte[PhysicsRegionFile.CHUNKS_PER_REGION][];
        }
    }

    private void scheduleFlush() {
        if (scheduledFlush == null || scheduledFlush.isDone()) {
            scheduledFlush = executor.schedule(this::flush, FLUSH_DELAY_MILLIS, TimeUnit.MILLISECONDS);
        }
    }

    private void flush() {
        for (Region region : regions.values()) {
            if (!region.dirty) {
                continue;
            }

            try {
                PhysicsRegionFile.write(region.path, region.chunks);
                region.dirty = false;
            } catch (IOException | RuntimeException e) {
                // Stays dirty, the next flush tries again
                Interactivity.LOGGER.error("Failed to write physics region {}", region.path, e);
            }
        }
    }

    private void evictIdle() {
        long now = System.currentTimeMillis();
        ObjectIterator<Long2ObjectMap.Entry<Region>> iterator = regions.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Region region = iterator.next().getValue();
            if (!region.dirty && now - region.lastAccess > IDLE_MILLIS) {
                iterator.remove();
            }
        }
    }

    private static int localIndex(long chunkKey) {
        return PhysicsRegionFile.localIndex(ChunkPos.getX(chunkKey), ChunkPos.getZ(chunkKey));
    }

    private static final class Region {
        private final Path path;
        private byte[][] chunks;
        private boolean dirty;
        private long lastAccess;

        private Region(Path path) {
            this.path = path;
        }
    }
}
