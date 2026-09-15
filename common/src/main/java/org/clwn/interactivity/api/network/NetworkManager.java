/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.network;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.clwn.interactivity.core.network.NetworkManagerImpl;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface NetworkManager {
    static NetworkManager getInstance() {
        return NetworkManagerImpl.INSTANCE;
    }

    /**
     * Registers a server-to-client packet type with the specified identifier and decoder function.
     * @param identifier Unique identifier for the packet type.
     * @param decoder Function that decodes a packet from the provided buffer.
     */
    <T extends NetworkPacket> void registerS2C(
            ResourceLocation identifier,
            Function<FriendlyByteBuf, T> decoder
    );

    /**
     * Registers a client-to-server packet type with the specified identifier and decoder function.
     * @param identifier Unique identifier for the packet type.
     * @param decoder Function that decodes a packet from the provided buffer.
     */
    <T extends NetworkPacket> void registerC2S(
            ResourceLocation identifier,
            Function<FriendlyByteBuf, T> decoder
    );

    default void sendPacketToPlayer(ServerPlayer player, NetworkPacket packet) {
        NetworkTransport transport = getPlayerTransport(player);

        if (transport != null) {
            transport.sendPacketToPlayer(player, packet);
        }
    }

    default void sendPacketToServer(NetworkPacket packet) {
        NetworkTransport transport = getCurrentTransport();

        if (transport != null) {
            transport.sendPacketToServer(packet);
        }

    }

    /**
     * Retrieves the network transport associated with the specified player.
     *
     * @param player The player for whom the network transport is to be retrieved.
     */
    @Nullable NetworkTransport getPlayerTransport(Player player);

    /**
     * Retrieves the current network transport in use by client.
     * @return The current network transport, or {@code null} if running on dedicated server.
     */
    @Nullable NetworkTransport getCurrentTransport();
}
