/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.network;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.api.InteractivityAPI;
import org.clwn.interactivity.api.physics.behavior.DataSyncBehavior;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.core.network.packet.s2c.S2CBodyDataPacket;
import org.clwn.interactivity.core.network.packet.s2c.S2CBodyRemovePacket;
import org.clwn.interactivity.core.network.packet.s2c.S2CBodySpawnPacket;
import org.clwn.interactivity.core.network.packet.s2c.S2CBodyStatesPacket;
import org.clwn.interactivity.core.physics.body.BodyChunkListener;
import org.clwn.interactivity.core.physics.body.BodyDataCodec;
import org.clwn.interactivity.core.physics.body.BodySpatialIndex;
import org.clwn.interactivity.core.physics.body.ServerBodyManager;
import org.clwn.interactivity.core.physics.body.store.SimulatedPhysicsStateContainer;
import org.clwn.interactivity.core.physics.world.ServerPhysicsWorldImpl;
import org.clwn.interactivity.core.util.platform.Services;
import org.clwn.interactivity.core.util.platform.services.INetworkingHelper;

import java.util.*;

public class NetworkDispatcher implements BodyChunkListener {
    private static final int MAX_SPAWNS_PER_PACKET = 128;
    private static final int MAX_REMOVES_PER_PACKET = 1024;
    private static final int MAX_DATA_BYTES_PER_PACKET = 1024;

    private final ServerPhysicsWorldImpl world;

    private volatile List<PlayerView> playerSnapshot = List.of();

    private final Map<UUID, PlayerTracker> trackers = new HashMap<>();

    private final Int2IntOpenHashMap networkIdToIndex = new Int2IntOpenHashMap();

    // Encoded records of the bodies that changed this tick, shared by all players
    private final FriendlyByteBuf recordBuffer = new FriendlyByteBuf(Unpooled.buffer(64 * 1024));
    private int[] recordNetworkId = new int[1024];
    private int[] recordStart = new int[1024];
    private int[] recordLength = new int[1024];
    private int recordCount;
    private final IntArrayList dataNetworkIds = new IntArrayList();
    private final List<byte[]> dataRecords = new ArrayList<>();


    public NetworkDispatcher(ServerPhysicsWorldImpl world) {
        this.world = world;
        this.networkIdToIndex.defaultReturnValue(-1);
    }

    public void publishPlayers(ServerLevel level) {
        List<ServerPlayer> players = level.players();
        List<PlayerView> views = new ArrayList<>(players.size());

        for (ServerPlayer player : players) {
            views.add(new PlayerView(player.getUUID(), player, player.getChunkTrackingView()));
        }

        this.playerSnapshot = views;
    }

    @Override
    public void onBodyAdded(int index, long chunkKey) {
        int networkId = world.getBodyManager().getBodyDataStore().getPhysicsStateContainer().networkId.get(index);
        networkIdToIndex.put(networkId, index);

        int chunkX = ChunkPos.getX(chunkKey);
        int chunkZ = ChunkPos.getZ(chunkKey);

        for (PlayerTracker tracker : trackers.values()) {
            if (tracker.view.contains(chunkX, chunkZ)) {
                tracker.track(networkId);
            }
        }
    }

    @Override
    public void onBodyRemoved(int index, long chunkKey) {
        int networkId = world.getBodyManager().getBodyDataStore().getPhysicsStateContainer().networkId.get(index);
        networkIdToIndex.remove(networkId);

        for (PlayerTracker tracker : trackers.values()) {
            tracker.untrack(networkId);
        }
    }

    public void update(int tick, SimulatedPhysicsStateContainer container) {
        updatePlayers();

        collectChangedData(container);

        if (trackers.isEmpty()) {
            return;
        }

        encodeChangedBodies(container);

        for (PlayerTracker tracker : trackers.values()) {
            sendSpawns(tracker, tick, container);
            sendRemovals(tracker);
            sendStates(tracker, tick);
            sendData(tracker);
        }
    }

    private void updatePlayers() {
        List<PlayerView> snapshot = this.playerSnapshot;

        Set<UUID> present = new HashSet<>();
        for (PlayerView view : snapshot) {
            present.add(view.uuid());
        }

        trackers.keySet().retainAll(present);

        for (PlayerView view : snapshot) {
            PlayerTracker tracker = trackers.get(view.uuid());

            if (
                    tracker == null
                            || tracker.player != view.player() // after respawning new player instance is being created
            ) {
                tracker = new PlayerTracker(view.player());
                trackers.put(view.uuid(), tracker);
            }

            if (!tracker.view.equals(view.view())) {
                updateView(tracker, view.view());
            }
        }
    }

    private void updateView(PlayerTracker tracker, ChunkTrackingView newView) {
        ChunkTrackingView oldView = tracker.view;
        tracker.view = newView;

        SimulatedPhysicsStateContainer container = world.getBodyManager().getBodyDataStore().getPhysicsStateContainer();
        BodySpatialIndex spatialIndex = world.getBodyManager().getChunkTracker().getSpatialIndex();
        LongIterator chunks = spatialIndex.occupiedChunks().iterator();
        while (chunks.hasNext()) {
            long chunkKey = chunks.nextLong();
            int chunkX = ChunkPos.getX(chunkKey);
            int chunkZ = ChunkPos.getZ(chunkKey);

            boolean wasInView = oldView.contains(chunkX, chunkZ);
            boolean isInView = newView.contains(chunkX, chunkZ);

            if (wasInView == isInView) {
                continue;
            }

            IntSet bodies = spatialIndex.bodiesIn(chunkKey);
            if (bodies == null) {
                continue;
            }

            IntIterator iterator = bodies.iterator();
            while (iterator.hasNext()) {
                int networkId = container.networkId.get(iterator.nextInt());

                if (isInView) {
                    tracker.track(networkId);
                } else {
                    tracker.untrack(networkId);
                }
            }
        }
    }

    @Override
    public void onBodyMoved(int index, long fromChunkKey, long toChunkKey) {
        int networkId = world.getBodyManager().getBodyDataStore().getPhysicsStateContainer().networkId.get(index);
        int chunkX = ChunkPos.getX(toChunkKey);
        int chunkZ = ChunkPos.getZ(toChunkKey);

        for (PlayerTracker tracker : trackers.values()) {
            boolean tracked = tracker.tracked.contains(networkId);
            boolean inView = tracker.view.contains(chunkX, chunkZ);

            if (inView && !tracked) {
                tracker.track(networkId);
            } else if (!inView && tracked) {
                tracker.untrack(networkId);
            }
        }
    }

    private void collectChangedData(SimulatedPhysicsStateContainer container) {
        dataNetworkIds.clear();
        dataRecords.clear();

        ServerBodyManager bodyManager = world.getBodyManager();
        ReferenceOpenHashSet<Body> seen = new ReferenceOpenHashSet<>();

        Body body;
        while ((body = bodyManager.pollDirtyDataBody()) != null) {
            if (!seen.add(body) || !body.getType().hasBehavior(DataSyncBehavior.class)) {
                continue;
            }

            // Removed, or not added yet (then its spawn carries the data)
            int index = bodyManager.getBodyDataStore().getIndexByUuid(body.getUuid());
            if (index == -1 || container.bodies[index] != body) {
                continue;
            }

            dataNetworkIds.add(container.networkId.get(index));
            dataRecords.add(BodyDataCodec.write(body));
        }
    }

    private void encodeChangedBodies(SimulatedPhysicsStateContainer container) {
        recordBuffer.clear();
        recordCount = 0;

        int dirtyCount = container.getDirtyCount();
        if (recordNetworkId.length < dirtyCount) {
            int capacity = Math.max(dirtyCount, recordNetworkId.length * 2);
            recordNetworkId = new int[capacity];
            recordStart = new int[capacity];
            recordLength = new int[capacity];
        }

        for (int k = 0; k < dirtyCount; k++) {
            int index = container.dirtyIndices.get(k);
            int networkId = container.networkId.get(index);

            // Slot was freed after it was marked
            if (networkId == -1) {
                continue;
            }

            int start = recordBuffer.writerIndex();
            BodyStateCodec.write(recordBuffer, container, index);

            recordNetworkId[recordCount] = networkId;
            recordStart[recordCount] = start;
            recordLength[recordCount] = recordBuffer.writerIndex() - start;
            recordCount++;
        }
    }

    private void sendSpawns(PlayerTracker tracker, int tick, SimulatedPhysicsStateContainer container) {
        tracker.spawnedThisTick.clear();

        if (tracker.pendingSpawn.isEmpty()) {
            return;
        }

        FriendlyByteBuf out = new FriendlyByteBuf(Unpooled.buffer());
        int count = 0;

        IntIterator iterator = tracker.pendingSpawn.iterator();
        while (iterator.hasNext()) {
            int networkId = iterator.nextInt();
            int index = networkIdToIndex.get(networkId);
            Body body = index == -1 ? null : container.bodies[index];

            if (body == null) {
                continue;
            }

            out.writeUUID(body.getUuid());
            out.writeResourceLocation(body.getType().getTypeId());
            BodyStateCodec.write(out, container, index);
            out.writeByteArray(BodyDataCodec.write(body));

            tracker.spawnedThisTick.add(networkId);
            count++;

            if (count == MAX_SPAWNS_PER_PACKET) {
                sendSpawnPacket(tracker.player, tick, count, out);
                out = new FriendlyByteBuf(Unpooled.buffer());
                count = 0;
            }
        }

        if (count > 0) {
            sendSpawnPacket(tracker.player, tick, count, out);
        }

        tracker.pendingSpawn.clear();
    }

    private void sendSpawnPacket(ServerPlayer player, int tick, int count, FriendlyByteBuf out) {
        byte[] data = new byte[out.readableBytes()];
        out.readBytes(data);

        sendMinecraftPacket(player, new S2CBodySpawnPacket(world.getServerLevel().dimension(), tick, count, data));
    }

    private void sendRemovals(PlayerTracker tracker) {
        if (tracker.pendingRemove.isEmpty()) {
            return;
        }

        IntArrayList batch = new IntArrayList(Math.min(tracker.pendingRemove.size(), MAX_REMOVES_PER_PACKET));

        IntIterator iterator = tracker.pendingRemove.iterator();
        while (iterator.hasNext()) {
            batch.add(iterator.nextInt());

            if (batch.size() == MAX_REMOVES_PER_PACKET) {
                sendMinecraftPacket(tracker.player, new S2CBodyRemovePacket(world.getServerLevel().dimension(), batch.toIntArray()));
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            sendMinecraftPacket(tracker.player, new S2CBodyRemovePacket(world.getServerLevel().dimension(), batch.toIntArray()));
        }

        tracker.pendingRemove.clear();
    }

    private void sendData(PlayerTracker tracker) {
        if (dataRecords.isEmpty()) {
            return;
        }

        FriendlyByteBuf out = new FriendlyByteBuf(Unpooled.buffer());
        int count = 0;

        for (int i = 0; i < dataRecords.size(); i++) {
            int networkId = dataNetworkIds.getInt(i);

            // Bodies spawned this tick got their data with the spawn
            if (!tracker.tracked.contains(networkId) || tracker.spawnedThisTick.contains(networkId)) {
                continue;
            }

            out.writeVarInt(networkId);
            out.writeByteArray(dataRecords.get(i));
            count++;

            if (out.readableBytes() >= MAX_DATA_BYTES_PER_PACKET) {
                sendDataPacket(tracker.player, count, out);
                out = new FriendlyByteBuf(Unpooled.buffer());
                count = 0;
            }
        }

        if (count > 0) {
            sendDataPacket(tracker.player, count, out);
        }
    }

    private void sendDataPacket(ServerPlayer player, int count, FriendlyByteBuf out) {
        byte[] data = new byte[out.readableBytes()];
        out.readBytes(data);

        sendMinecraftPacket(player, new S2CBodyDataPacket(world.getServerLevel().dimension(), count, data));
    }

    private void sendStates(PlayerTracker tracker, int tick) {
        byte[] data = new byte[S2CBodyStatesPacket.MAX_DATA_BYTES];
        int length = 0;
        int count = 0;

        for (int r = 0; r < recordCount; r++) {
            int networkId = recordNetworkId[r];

            if (!tracker.tracked.contains(networkId) || tracker.spawnedThisTick.contains(networkId)) {
                continue;
            }

            int recordSize = recordLength[r];

            if (length + recordSize > data.length) {
                sendStatesPacket(tracker.player, tick, count, data, length);
                data = new byte[S2CBodyStatesPacket.MAX_DATA_BYTES];
                length = 0;
                count = 0;
            }

            recordBuffer.getBytes(recordStart[r], data, length, recordSize);
            length += recordSize;
            count++;
        }

        if (count > 0) {
            sendStatesPacket(tracker.player, tick, count, data, length);
        }
    }

    private void sendStatesPacket(ServerPlayer player, int tick, int count, byte[] data, int length) {
        try {
            InteractivityAPI.getInstance().getNetworkManager().sendPacketToPlayer(player, new S2CBodyStatesPacket(tick, count, data, length));
        } catch (Exception e) {
            Interactivity.LOGGER.error("Failed to send body states to {}", player.getName().getString(), e);
        }
    }

    private void sendMinecraftPacket(ServerPlayer player, CustomPacketPayload packet) {
        try {
            Services.PLATFORM_NETWORK.sendPacketToPlayer(player, packet, INetworkingHelper.PacketStageType.PLAY, null);
        } catch (Exception e) {
            Interactivity.LOGGER.error("Failed to send {} to {}", packet.type().id(), player.getName().getString(), e);
        }
    }
}
