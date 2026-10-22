/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.client.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.clwn.interactivity.core.client.physics.ClientPhysicsPipelineHelper;
import org.clwn.interactivity.core.client.physics.body.ClientBodyManager;
import org.clwn.interactivity.core.client.physics.world.ClientPhysicsWorldInternal;
import org.clwn.interactivity.core.network.packet.s2c.S2CBodyRemovePacket;
import org.clwn.interactivity.core.network.packet.s2c.S2CBodySpawnPacket;
import org.clwn.interactivity.core.network.packet.s2c.S2CPhysicsConfigPacket;
import org.clwn.interactivity.core.physics.PhysicsPipelineImpl;
import org.clwn.interactivity.core.physics.body.type.BodyTypeRegistryImpl;
import org.clwn.interactivity.core.physics.network.BodyStateCodec;

import java.util.Optional;
import java.util.UUID;

/**
 * Applies the physics packets sent by the server to the physics world of the current client level.
 */
public final class ClientBodySyncHandler {
    private ClientBodySyncHandler() {}

    public static void handleConfig(S2CPhysicsConfigPacket packet) {
        PhysicsPipelineImpl.INSTANCE.handleConfig(packet);
    }

    public static void handleSpawn(S2CBodySpawnPacket packet) {
        ClientPhysicsWorldInternal world = ClientPhysicsPipelineHelper.getCurrentPhysicsWorld();

        // In-flight packet of a level the client has already left
        if (world == null || !world.getClientLevel().dimension().equals(packet.dimension())) {
            return;
        }

        ClientBodyManager manager = world.getBodyManager();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(packet.data()));
        BodyStateCodec.Record state = new BodyStateCodec.Record();

        for (int i = 0; i < packet.count(); i++) {
            UUID uuid = buf.readUUID();
            ResourceLocation typeId = buf.readResourceLocation();
            state.read(buf);

            Optional<BodyType<? extends Body>> type = BodyTypeRegistryImpl.INSTANCE.get(typeId);
            if (type.isEmpty()) {
                Interactivity.LOGGER.warn("Server spawned a body of unknown type {}", typeId);
                continue;
            }

            manager.addRemote(state.networkId, uuid, type.get(), state, packet.tick());
        }
    }

    public static void handleRemove(S2CBodyRemovePacket packet) {
        ClientPhysicsWorldInternal world = ClientPhysicsPipelineHelper.getCurrentPhysicsWorld();

        if (world == null || !world.getClientLevel().dimension().equals(packet.dimension())) {
            return;
        }

        for (int networkId : packet.networkIds()) {
            world.getBodyManager().removeRemote(networkId);
        }
    }

    public static void handleStates(int tick, int count, byte[] data, int length) {
        ClientPhysicsWorldInternal world = ClientPhysicsPipelineHelper.getCurrentPhysicsWorld();
        if (world == null) {
            return;
        }

        ClientBodyManager manager = world.getBodyManager();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data, 0, length));
        BodyStateCodec.Record state = new BodyStateCodec.Record();

        for (int i = 0; i < count; i++) {
            state.read(buf);
            manager.applyState(state, tick);
        }

        world.onServerTick(tick);
    }
}
