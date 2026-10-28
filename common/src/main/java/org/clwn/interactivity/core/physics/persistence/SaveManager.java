/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.persistence;

import com.github.stephengold.joltjni.*;
import com.github.stephengold.joltjni.enumerate.EActivation;
import com.github.stephengold.joltjni.enumerate.EStreamType;
import com.github.stephengold.joltjni.std.StringStream;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.api.physics.behavior.PersistenceBehavior;
import org.clwn.interactivity.api.physics.body.AddReason;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.clwn.interactivity.api.physics.body.RemovalReason;
import org.clwn.interactivity.core.physics.body.BodyChunkListener;
import org.clwn.interactivity.core.physics.body.BodyDataCodec;
import org.clwn.interactivity.core.physics.body.ServerBodyManager;
import org.clwn.interactivity.core.physics.body.store.SimulatedBodyDataStore;
import org.clwn.interactivity.core.physics.body.store.SimulatedPhysicsStateContainer;
import org.clwn.interactivity.core.physics.body.type.BodyTypeRegistryImpl;
import org.clwn.interactivity.core.physics.constraint.ConstraintImpl;
import org.clwn.interactivity.core.physics.persistence.storage.PhysicsRegionStorage;
import org.clwn.interactivity.core.physics.world.ServerPhysicsWorldImpl;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Saves bodies and their constraints together with the chunks they are in.
 * <p>
 * The unit of saving is a group: bodies connected by constraints (a single body is a group of one). A group is always
 * written as one {@link GroupRecord} into the payload of one chunk, and it is only simulated while every chunk of its
 * footprint is loaded.
 * <p>
 * When a chunk unloads, every group with a body in it is written into that chunk and removed.
 */
// todo: this is some hard-readable slop that I gonna simplify in future, because it takes time to figure out logic
public class SaveManager implements BodyChunkListener {
    private static final long NOT_STORED = Long.MAX_VALUE;
    private static final long TIMEOUT_SECONDS = 60;

    private final ServerPhysicsWorldImpl world;

    @Getter
    private final PhysicsRegionStorage storage;

    private volatile boolean closed;

    private final LongOpenHashSet readyChunks = new LongOpenHashSet();
    private final LongOpenHashSet readingChunks = new LongOpenHashSet();

    /**
     * Groups unloaded into a chunk while its payload was still being read.
     */
    private final Long2ObjectOpenHashMap<ObjectArrayList<byte[]>> pendingUnloads = new Long2ObjectOpenHashMap<>();

    /**
     * The chunk whose payload contains a live persistent body.
     */
    private final Object2LongOpenHashMap<UUID> storedChunk = new Object2LongOpenHashMap<>();
    private final Long2ObjectOpenHashMap<ObjectOpenHashSet<UUID>> storedBodies = new Long2ObjectOpenHashMap<>();

    private final Long2ObjectOpenHashMap<ObjectArrayList<WaitingGroup>> waitingByStoredChunk = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<ObjectArrayList<WaitingGroup>> waitingByFootprintChunk = new Long2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<UUID, WaitingGroup> waitingByBody = new Object2ObjectOpenHashMap<>();

    /**
     * Chunks whose payload has to be rewritten.
     */
    private final LongOpenHashSet dirtyChunks = new LongOpenHashSet();

    /**
     * Bodies that entered a chunk that is not loaded, mapped to the chunk they came from.
     */
    private final Object2LongOpenHashMap<UUID> leavingBodies = new Object2LongOpenHashMap<>();

    private final ObjectOpenHashSet<ResourceLocation> warnedTypes = new ObjectOpenHashSet<>();

    public SaveManager(ServerPhysicsWorldImpl world, Path folder) {
        this.world = world;
        this.storage = new PhysicsRegionStorage(folder, "Interactivity Physics IO - " + world.getServerLevel().dimension().location().getPath());
        this.storedChunk.defaultReturnValue(NOT_STORED);
        this.leavingBodies.defaultReturnValue(NOT_STORED);
    }

    public void onChunkLoad(long chunk) {
        if (!closed) {
            world.execute(() -> load(chunk));
        }
    }

    public void onChunkUnload(long chunk) {
        if (!closed) {
            world.execute(() -> unload(chunk));
        }
    }

    public void onLevelSave(boolean flush) {
        if (closed || !world.isRunning()) {
            return;
        }

        CompletableFuture<Void> saved = new CompletableFuture<>();
        world.execute(() -> {
            try {
                saveAll();
                saved.complete(null);
            } catch (Exception e) {
                saved.completeExceptionally(e);
            }
        });

        if (flush) {
            await(saved.thenCompose(result -> storage.flushAll()), "save physics data");
        }
    }

    /**
     * Saves everything and stops the storage. Blocks until done.
     *
     * @param onPhysicsThread {@code true} if called by the physics thread itself
     */
    public void close(boolean onPhysicsThread) {
        if (closed) {
            return;
        }
        closed = true;

        if (onPhysicsThread) {
            try {
                saveForClose();
            } catch (Exception e) {
                Interactivity.LOGGER.error("Failed to save physics data", e);
            }
        } else if (world.isRunning()) {
            CompletableFuture<Void> saved = new CompletableFuture<>();
            world.execute(() -> {
                try {
                    saveForClose();
                    saved.complete(null);
                } catch (Exception e) {
                    saved.completeExceptionally(e);
                }
            });
            await(saved, "save physics data");
        } else {
            Interactivity.LOGGER.error("Physics data of {} was not saved, the physics thread is not running", world.getServerLevel().dimension().location());
        }

        storage.close();
    }

    public void saveAll() {
        processLeavingBodies();

        SimulatedBodyDataStore store = world.getBodyManager().getBodyDataStore();
        SimulatedPhysicsStateContainer container = store.getPhysicsStateContainer();
        ObjectOpenHashSet<UUID> visited = new ObjectOpenHashSet<>();

        for (int i = 0; i < store.getSlotCount(); i++) {
            Body body = container.bodies[i];
            if (body == null || visited.contains(body.getUuid())) {
                continue;
            }

            Group group = collect(body.getUuid());
            visited.addAll(group.bodies());

            long home = smallestReadyChunk(group);
            if (home != NOT_STORED) {
                storeGroup(group, home);
            }
        }

        writeDirtyChunks();
    }

    /**
     * Called after every physics tick, once the chunks of the bodies are up to date.
     */
    public void afterTick() {
        processLeavingBodies();

        if (!dirtyChunks.isEmpty()) {
            writeDirtyChunks();
        }
    }

    public void onBodyRemoved(Body body, RemovalReason removalReason) {
        UUID uuid = body.getUuid();
        leavingBodies.removeLong(uuid);

        if (removalReason == RemovalReason.DISCARD) {
            clearStored(uuid);
        } else {
            // Whoever unloads it already decided where it is saved
            forgetStored(uuid);
        }
    }

    @Override
    public void onBodyAdded(int index, long chunkKey) {
        if (!readyChunks.contains(chunkKey)) {
            leavingBodies.put(uuidAt(index), NOT_STORED);
        }
    }

    @Override
    public void onBodyMoved(int index, long fromChunkKey, long toChunkKey) {
        if (!readyChunks.contains(toChunkKey)) {
            leavingBodies.put(uuidAt(index), fromChunkKey);
        }
    }

    private void load(long chunk) {
        if (!readyChunks.add(chunk)) {
            return;
        }

        retryWaiting(chunk);

        if (readingChunks.add(chunk)) {
            storage.read(chunk).whenComplete((payload, error) -> world.execute(() -> applyPayload(chunk, payload, error)));
        }
    }

    private void applyPayload(long chunk, byte[] payload, @Nullable Throwable error) {
        if (!readingChunks.remove(chunk)) {
            return;
        }

        ObjectArrayList<byte[]> groups = new ObjectArrayList<>();

        // Unloaded groups are newer than the payload, so they go first and win over duplicates in it
        ObjectArrayList<byte[]> pending = pendingUnloads.remove(chunk);
        if (pending != null) {
            groups.addAll(pending);
        }

        if (error != null) {
            Interactivity.LOGGER.error("Failed to read physics data of chunk {}", new ChunkPos(chunk), error);
        } else if (payload != null) {
            try {
                groups.addAll(ChunkRecordCodec.decodeChunk(payload));
            } catch (RuntimeException e) {
                Interactivity.LOGGER.error("Physics data of chunk {} is corrupt and was dropped", new ChunkPos(chunk), e);
            }
        }

        if (!readyChunks.contains(chunk)) {
            // Unloaded again before the payload arrived
            storage.write(chunk, ChunkRecordCodec.encodeChunk(withoutDuplicates(chunk, groups)));
            return;
        }

        ObjectArrayList<WaitingGroup> loaded = new ObjectArrayList<>();
        for (byte[] raw : groups) {
            WaitingGroup group = decode(chunk, raw);
            if (group == null) {
                dirtyChunks.add(chunk);
                continue;
            }

            if (isKnown(group.record())) {
                Interactivity.LOGGER.warn("Dropped a saved group of chunk {}, its bodies are already loaded", new ChunkPos(chunk));
                dirtyChunks.add(chunk);
                continue;
            }

            addWaiting(group);
            loaded.add(group);
        }

        // The payload on disk does not contain them yet
        if (pending != null) {
            dirtyChunks.add(chunk);
        }

        for (WaitingGroup group : loaded) {
            tryActivate(group);
        }

        writeDirtyChunks();
    }

    private void unload(long chunk) {
        if (!readyChunks.remove(chunk)) {
            return;
        }

        ObjectArrayList<byte[]> records = new ObjectArrayList<>();
        ObjectOpenHashSet<UUID> visited = new ObjectOpenHashSet<>();

        // Groups with a body in the chunk are stored in it
        IntSet indices = world.getBodyManager().getChunkTracker().getSpatialIndex().bodiesIn(chunk);
        if (indices != null) {
            for (UUID uuid : uuidsAt(indices)) {
                if (visited.contains(uuid)) {
                    continue;
                }

                Group group = collect(uuid);
                visited.addAll(group.bodies());
                unloadGroup(group, records);
            }
        }

        // Groups stored in the chunk without a body in it move to another chunk of their footprint
        ObjectOpenHashSet<UUID> stored = storedBodies.get(chunk);
        if (stored != null) {
            for (UUID uuid : stored.toArray(new UUID[0])) {
                if (visited.contains(uuid)) {
                    continue;
                }

                Group group = collect(uuid);
                visited.addAll(group.bodies());

                long home = smallestReadyChunk(group);
                if (home == NOT_STORED) {
                    unloadGroup(group, records);
                } else {
                    storeGroup(group, home);
                }
            }
        }

        ObjectArrayList<WaitingGroup> waiting = waitingByStoredChunk.get(chunk);
        if (waiting != null) {
            for (WaitingGroup group : waiting.toArray(new WaitingGroup[0])) {
                records.add(group.raw());
                removeWaiting(group);
            }
        }

        dirtyChunks.remove(chunk);
        if (readingChunks.contains(chunk)) {
            pendingUnloads.computeIfAbsent(chunk, key -> new ObjectArrayList<>()).addAll(records);
        } else {
            storage.write(chunk, ChunkRecordCodec.encodeChunk(records));
        }

        writeDirtyChunks();
    }

    /**
     * Saves the groups of bodies that entered a chunk that is not loaded into a loaded chunk and removes them.
     * They come back once their whole footprint is loaded.
     */
    private void processLeavingBodies() {
        if (leavingBodies.isEmpty()) {
            return;
        }

        Object2LongOpenHashMap<UUID> leaving = new Object2LongOpenHashMap<>(leavingBodies);
        leavingBodies.clear();

        SimulatedBodyDataStore store = world.getBodyManager().getBodyDataStore();
        ObjectOpenHashSet<UUID> visited = new ObjectOpenHashSet<>();

        for (Object2LongMap.Entry<UUID> entry : leaving.object2LongEntrySet()) {
            UUID uuid = entry.getKey();
            int index = store.getIndexByUuid(uuid);
            if (index == -1 || visited.contains(uuid) || readyChunks.contains(store.getPhysicsStateContainer().chunkKey.get(index))) {
                continue;
            }

            Group group = collect(uuid);
            visited.addAll(group.bodies());

            long home = smallestReadyChunk(group);
            if (home == NOT_STORED) {
                long from = entry.getLongValue();
                if (!readyChunks.contains(from)) {
                    // There is no loaded chunk to save it into, keep simulating it
                    continue;
                }
                home = from;
            }

            GroupRecord record = snapshot(group);
            removeGroup(group);

            if (record != null) {
                addWaiting(new WaitingGroup(home, ChunkRecordCodec.encodeGroup(record), record, false));
                dirtyChunks.add(home);
            }
        }
    }

    /**
     * Applies the payloads that are still being read and saves everything. Runs on the physics thread before closing.
     */
    private void saveForClose() {
        for (long chunk : readingChunks.toLongArray()) {
            applyPayload(chunk, storage.read(chunk).join(), null);
        }
        saveAll();
    }

    private void retryWaiting(long chunk) {
        ObjectArrayList<WaitingGroup> groups = waitingByFootprintChunk.get(chunk);
        if (groups == null) {
            return;
        }

        for (WaitingGroup group : groups.toArray(new WaitingGroup[0])) {
            tryActivate(group);
        }
    }

    /**
     * Spawns a waiting group if its whole footprint is loaded.
     */
    private void tryActivate(WaitingGroup group) {
        if (group.dormant()) {
            return;
        }
        for (long chunk : group.record().footprint()) {
            if (!readyChunks.contains(chunk)) {
                return;
            }
        }

        removeWaiting(group);

        ServerBodyManager bodyManager = world.getBodyManager();
        GroupRecord.BodyRecord[] bodies = group.record().bodies();
        UUID[] spawned = new UUID[bodies.length];

        for (int i = 0; i < bodies.length; i++) {
            GroupRecord.BodyRecord record = bodies[i];
            BodyType<? extends Body> type = BodyTypeRegistryImpl.INSTANCE.get(record.type()).orElseThrow();

            Body body = type.create(world, record.uuid());
            BodyDataCodec.read(body, record.data());
            double[] position = record.position();
            float[] rotation = record.rotation();
            float[] linearVelocity = record.linearVelocity();
            float[] angularVelocity = record.angularVelocity();

            boolean added = bodyManager.spawnBodyInternal(
                    body,
                    position[0], position[1], position[2],
                    rotation[0], rotation[1], rotation[2], rotation[3],
                    linearVelocity[0], linearVelocity[1], linearVelocity[2],
                    angularVelocity[0], angularVelocity[1], angularVelocity[2],
                    record.awake() ? EActivation.Activate : EActivation.DontActivate,
                    AddReason.LOADED
            );

            if (added) {
                spawned[i] = record.uuid();
                if (isPersistent(type)) {
                    setStored(record.uuid(), group.storedChunk());
                }
            }
        }

        for (GroupRecord.ConstraintRecord record : group.record().constraints()) {
            UUID body1 = spawned[record.body1()];
            UUID body2 = record.body2() == GroupRecord.ConstraintRecord.WORLD ? null : spawned[record.body2()];
            if (body1 == null || (record.body2() != GroupRecord.ConstraintRecord.WORLD && body2 == null)) {
                Interactivity.LOGGER.warn("Constraint {} was not restored, one of its bodies failed to load", record.uuid());
                continue;
            }

            try (TwoBodyConstraintSettingsRef settings = readSettings(record.settings())) {
                if (settings == null) {
                    Interactivity.LOGGER.warn("Constraint {} was not restored, its settings are corrupt", record.uuid());
                    continue;
                }
                world.getConstraintManager().restoreConstraint(record.uuid(), body1, body2, settings.getPtr(), record.state());
            }
        }
    }

    /**
     * Writes the dirty chunks. Chunks whose payload is still being read stay dirty until it arrives.
     */
    private void writeDirtyChunks() {
        boolean progress = true;
        while (progress && !dirtyChunks.isEmpty()) {
            progress = false;

            for (long chunk : dirtyChunks.toLongArray()) {
                if (!dirtyChunks.contains(chunk) || readingChunks.contains(chunk)) {
                    continue;
                }
                if (!readyChunks.contains(chunk)) {
                    dirtyChunks.remove(chunk);
                    continue;
                }

                writeChunk(chunk);
                progress = true;
            }
        }
    }

    /**
     * Rewrites the payload of a loaded chunk: the live groups whose home is this chunk and the waiting groups stored in it.
     * The home of a live group is the smallest chunk any of its bodies is stored in.
     */
    private void writeChunk(long chunk) {
        ObjectArrayList<byte[]> groups = new ObjectArrayList<>();

        ObjectOpenHashSet<UUID> stored = storedBodies.get(chunk);
        if (stored != null) {
            ObjectOpenHashSet<UUID> visited = new ObjectOpenHashSet<>();

            for (UUID uuid : stored.toArray(new UUID[0])) {
                if (visited.contains(uuid)) {
                    continue;
                }

                Group group = collect(uuid);
                visited.addAll(group.bodies());

                long home = homeOf(group);
                storeGroup(group, home);
                if (home != chunk) {
                    continue;
                }

                GroupRecord record = snapshot(group);
                if (record != null) {
                    groups.add(ChunkRecordCodec.encodeGroup(record));
                }
            }
        }

        ObjectArrayList<WaitingGroup> waiting = waitingByStoredChunk.get(chunk);
        if (waiting != null) {
            for (WaitingGroup group : waiting) {
                groups.add(group.raw());
            }
        }

        // Moving bodies out of this chunk marked it again, but the payload below already reflects that
        dirtyChunks.remove(chunk);
        storage.write(chunk, ChunkRecordCodec.encodeChunk(groups));
    }

    private void unloadGroup(Group group, List<byte[]> records) {
        GroupRecord record = snapshot(group);
        if (record != null) {
            records.add(ChunkRecordCodec.encodeGroup(record));
        }
        removeGroup(group);
    }

    private void removeGroup(Group group) {
        ServerBodyManager bodyManager = world.getBodyManager();
        for (UUID uuid : group.bodies()) {
            clearStored(uuid);
            bodyManager.removeBodyInternal(uuid, RemovalReason.UNLOAD);
        }
    }

    /**
     * Moves the persistent bodies of a group into the payload of a chunk.
     */
    private void storeGroup(Group group, long chunk) {
        SimulatedBodyDataStore store = world.getBodyManager().getBodyDataStore();
        boolean stored = false;

        for (UUID uuid : group.bodies()) {
            int index = store.getIndexByUuid(uuid);
            if (index != -1 && isPersistent(store.getPhysicsStateContainer().bodies[index].getType())) {
                setStored(uuid, chunk);
                stored = true;
            }
        }

        if (stored) {
            dirtyChunks.add(chunk);
        }
    }

    /**
     * Encodes the current state of the persistent bodies of a group and the constraints between them.
     *
     * @return the record, or {@code null} if the group has no persistent bodies
     */
    private @Nullable GroupRecord snapshot(Group group) {
        SimulatedBodyDataStore store = world.getBodyManager().getBodyDataStore();
        SimulatedPhysicsStateContainer container = store.getPhysicsStateContainer();

        ObjectArrayList<GroupRecord.BodyRecord> bodies = new ObjectArrayList<>();
        Object2IntOpenHashMap<UUID> bodyIndices = new Object2IntOpenHashMap<>();
        bodyIndices.defaultReturnValue(-2);
        LongLinkedOpenHashSet footprint = new LongLinkedOpenHashSet();

        for (UUID uuid : group.bodies()) {
            int index = store.getIndexByUuid(uuid);
            if (index == -1) {
                continue;
            }

            Body body = container.bodies[index];
            if (!isPersistent(body.getType())) {
                continue;
            }

            bodyIndices.put(uuid, bodies.size());
            bodies.add(new GroupRecord.BodyRecord(
                    uuid,
                    body.getType().getTypeId(),
                    new double[]{container.posX.get(index), container.posY.get(index), container.posZ.get(index)},
                    new float[]{container.rotX.get(index), container.rotY.get(index), container.rotZ.get(index), container.rotW.get(index)},
                    new float[]{container.velX.get(index), container.velY.get(index), container.velZ.get(index)},
                    new float[]{container.angVelX.get(index), container.angVelY.get(index), container.angVelZ.get(index)},
                    container.isActive.get(index) == 1,
                    BodyDataCodec.write(body)
            ));
            footprint.add(container.chunkKey.get(index));
        }

        if (bodies.isEmpty()) {
            return null;
        }

        ObjectArrayList<GroupRecord.ConstraintRecord> constraints = new ObjectArrayList<>();
        for (ConstraintImpl<?> constraint : group.constraints()) {
            int body1 = bodyIndices.getInt(constraint.getBody1());
            int body2 = constraint.getBody2() == null ? GroupRecord.ConstraintRecord.WORLD : bodyIndices.getInt(constraint.getBody2());
            TwoBodyConstraint joltConstraint = constraint.getJoltConstraint();

            // Constraints to bodies that are not saved are not saved either
            if (body1 < 0 || body2 < GroupRecord.ConstraintRecord.WORLD || joltConstraint == null) {
                continue;
            }

            byte[] settings = writeSettings(joltConstraint);
            if (settings == null) {
                Interactivity.LOGGER.warn("Constraint {} was not saved, its settings could not be written", constraint.getUuid());
                continue;
            }

            constraints.add(new GroupRecord.ConstraintRecord(constraint.getUuid(), body1, body2, settings, writeState(joltConstraint)));
        }

        return new GroupRecord(
                footprint.toLongArray(),
                bodies.toArray(new GroupRecord.BodyRecord[0]),
                constraints.toArray(new GroupRecord.ConstraintRecord[0])
        );
    }

    private @Nullable WaitingGroup decode(long chunk, byte[] raw) {
        GroupRecord record;
        try {
            record = ChunkRecordCodec.decodeGroup(raw);
        } catch (RuntimeException e) {
            Interactivity.LOGGER.error("A saved group of chunk {} is corrupt and was dropped", new ChunkPos(chunk), e);
            return null;
        }

        boolean dormant = false;
        for (GroupRecord.BodyRecord body : record.bodies()) {
            if (!BodyTypeRegistryImpl.INSTANCE.contains(body.type())) {
                dormant = true;
                if (warnedTypes.add(body.type())) {
                    Interactivity.LOGGER.warn("Unknown body type {}, its saved groups are kept but not loaded", body.type());
                }
            }
        }

        return new WaitingGroup(chunk, raw, record, dormant);
    }

    private List<byte[]> withoutDuplicates(long chunk, List<byte[]> groups) {
        ObjectOpenHashSet<UUID> seen = new ObjectOpenHashSet<>();
        ObjectArrayList<byte[]> result = new ObjectArrayList<>();

        for (byte[] raw : groups) {
            WaitingGroup group = decode(chunk, raw);
            if (group == null) {
                continue;
            }

            boolean duplicate = false;
            for (GroupRecord.BodyRecord body : group.record().bodies()) {
                duplicate |= !seen.add(body.uuid());
            }

            if (duplicate) {
                Interactivity.LOGGER.warn("Dropped a saved group of chunk {}, its bodies are saved twice", new ChunkPos(chunk));
            } else {
                result.add(raw);
            }
        }
        return result;
    }

    private boolean isKnown(GroupRecord record) {
        SimulatedBodyDataStore store = world.getBodyManager().getBodyDataStore();
        for (GroupRecord.BodyRecord body : record.bodies()) {
            if (store.getIndexByUuid(body.uuid()) != -1 || waitingByBody.containsKey(body.uuid())) {
                return true;
            }
        }
        return false;
    }

    private void addWaiting(WaitingGroup group) {
        waitingByStoredChunk.computeIfAbsent(group.storedChunk(), key -> new ObjectArrayList<>()).add(group);
        for (long chunk : group.record().footprint()) {
            waitingByFootprintChunk.computeIfAbsent(chunk, key -> new ObjectArrayList<>()).add(group);
        }
        for (GroupRecord.BodyRecord body : group.record().bodies()) {
            waitingByBody.put(body.uuid(), group);
        }
    }

    private void removeWaiting(WaitingGroup group) {
        removeFrom(waitingByStoredChunk, group.storedChunk(), group);
        for (long chunk : group.record().footprint()) {
            removeFrom(waitingByFootprintChunk, chunk, group);
        }
        for (GroupRecord.BodyRecord body : group.record().bodies()) {
            waitingByBody.remove(body.uuid());
        }
    }

    private static void removeFrom(Long2ObjectOpenHashMap<ObjectArrayList<WaitingGroup>> map, long chunk, WaitingGroup group) {
        ObjectArrayList<WaitingGroup> groups = map.get(chunk);
        if (groups != null && groups.remove(group) && groups.isEmpty()) {
            map.remove(chunk);
        }
    }

    private void setStored(UUID body, long chunk) {
        long old = storedChunk.put(body, chunk);
        if (old == chunk) {
            return;
        }

        if (old != NOT_STORED) {
            removeStoredBody(old, body);
            dirtyChunks.add(old);
        }
        storedBodies.computeIfAbsent(chunk, key -> new ObjectOpenHashSet<>()).add(body);
    }

    /**
     * Removes a body from the payload it is in.
     */
    private void clearStored(UUID body) {
        long old = storedChunk.removeLong(body);
        if (old != NOT_STORED) {
            removeStoredBody(old, body);
            dirtyChunks.add(old);
        }
    }

    /**
     * Stops tracking where a body is saved without changing any payload.
     */
    private void forgetStored(UUID body) {
        long old = storedChunk.removeLong(body);
        if (old != NOT_STORED) {
            removeStoredBody(old, body);
        }
    }

    private void removeStoredBody(long chunk, UUID body) {
        ObjectOpenHashSet<UUID> bodies = storedBodies.get(chunk);
        if (bodies != null && bodies.remove(body) && bodies.isEmpty()) {
            storedBodies.remove(chunk);
        }
    }

    private Group collect(UUID seed) {
        ObjectOpenHashSet<UUID> bodies = new ObjectOpenHashSet<>();
        ObjectArrayList<ConstraintImpl<?>> constraints = new ObjectArrayList<>();
        world.getConstraintManager().collectGroup(seed, bodies, constraints);
        return new Group(bodies, constraints);
    }

    /**
     * @return the smallest chunk a persistent body of the group is stored in, or {@link #NOT_STORED}
     */
    private long homeOf(Group group) {
        long home = NOT_STORED;
        for (UUID uuid : group.bodies()) {
            home = smallerChunk(home, storedChunk.getLong(uuid));
        }
        return home;
    }

    /**
     * @return the smallest loaded chunk a persistent body of the group is in, or {@link #NOT_STORED}
     */
    private long smallestReadyChunk(Group group) {
        SimulatedBodyDataStore store = world.getBodyManager().getBodyDataStore();
        SimulatedPhysicsStateContainer container = store.getPhysicsStateContainer();

        long smallest = NOT_STORED;
        for (UUID uuid : group.bodies()) {
            int index = store.getIndexByUuid(uuid);
            if (index == -1 || !isPersistent(container.bodies[index].getType())) {
                continue;
            }

            long chunk = container.chunkKey.get(index);
            if (readyChunks.contains(chunk)) {
                smallest = smallerChunk(smallest, chunk);
            }
        }
        return smallest;
    }

    /**
     * Orders chunks by x, then z. {@link #NOT_STORED} is larger than every chunk.
     */
    private static long smallerChunk(long a, long b) {
        if (a == NOT_STORED) {
            return b;
        }
        if (b == NOT_STORED) {
            return a;
        }

        int compare = Integer.compare(ChunkPos.getX(a), ChunkPos.getX(b));
        if (compare == 0) {
            compare = Integer.compare(ChunkPos.getZ(a), ChunkPos.getZ(b));
        }
        return compare <= 0 ? a : b;
    }

    private static boolean isPersistent(BodyType<?> type) {
        return type.hasBehavior(PersistenceBehavior.class);
    }

    private UUID uuidAt(int index) {
        return world.getBodyManager().getBodyDataStore().getPhysicsStateContainer().bodies[index].getUuid();
    }

    private UUID[] uuidsAt(IntSet indices) {
        UUID[] uuids = new UUID[indices.size()];
        int i = 0;
        for (int index : indices) {
            uuids[i++] = uuidAt(index);
        }
        return uuids;
    }

    /**
     * Writes the live settings of a constraint as a Jolt text object stream.
     * Text is used because the stream is passed through a Java string, which would not keep binary data intact.
     */
    private static byte[] writeSettings(TwoBodyConstraint constraint) {
        try (ConstraintSettingsRef settings = constraint.getConstraintSettings(); StringStream stream = new StringStream()) {
            if (!ObjectStreamOut.sWriteObject(stream, EStreamType.Text, settings.getPtr())) {
                return null;
            }
            return stream.str().getBytes(StandardCharsets.UTF_8);
        }
    }

    private static byte[] writeState(TwoBodyConstraint constraint) {
        try (StateRecorderImpl recorder = new StateRecorderImpl()) {
            constraint.saveState(recorder);
            return recorder.getData();
        }
    }

    private static @Nullable TwoBodyConstraintSettingsRef readSettings(byte[] data) {
        TwoBodyConstraintSettingsRef settings = new TwoBodyConstraintSettingsRef();
        try (StringStream stream = new StringStream(new String(data, StandardCharsets.UTF_8))) {
            if (ObjectStreamIn.sReadObject(stream, settings) && settings.getPtr() != null) {
                return settings;
            }
        } catch (RuntimeException e) {
            Interactivity.LOGGER.warn("Failed to read constraint settings", e);
        }
        settings.close();
        return null;
    }

    private static void await(CompletableFuture<?> future, String action) {
        try {
            future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            Interactivity.LOGGER.error("Failed to {}", action, e);
        }
    }

    private record Group(ObjectOpenHashSet<UUID> bodies, ObjectArrayList<ConstraintImpl<?>> constraints) {
    }

    /**
     * A loaded group whose footprint is not loaded completely yet, or whose body type is unknown ({@code dormant}).
     *
     * @param storedChunk the chunk whose payload contains it
     * @param raw         the encoded group, written back unchanged
     */
    private record WaitingGroup(long storedChunk, byte[] raw, GroupRecord record, boolean dormant) {
    }
}
