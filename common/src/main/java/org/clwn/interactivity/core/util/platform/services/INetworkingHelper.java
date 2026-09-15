/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.util.platform.services;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.clwn.interactivity.api.network.PacketContext;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * Universal interface for networking helpers across different platforms.
 */
/* todo: blow this shit up and rework fucking everything (related to configuration phase)
    or do this only for neoforge configuration code idk
*/
public interface INetworkingHelper {
    enum PacketStageType {
        COMMON,
        CONFIGURATION,
        PLAY
    }

    enum PacketType {
        BIDIRECTIONAL,
        S2C,
        C2S
    }

    <T extends CustomPacketPayload> void registerPacket(
            PacketStageType stage,
            PacketType packetType,
            CustomPacketPayload.Type<T> type,
            StreamCodec<FriendlyByteBuf, T> codec,
            BiConsumer<PacketContext, T> handler
            );

    /**
     * Sends packet to player
     * @param player Packet receiver
     * @param packet Network packet
     * @param stage Packet type (only used in fabric implementation)
     * @param serverConfigurationPacketListener Server configuration packet listener (only used in fabric implementation, required to send configuration packet)
     */
    void sendPacketToPlayer(ServerPlayer player, CustomPacketPayload packet, PacketStageType stage, @Nullable ServerConfigurationPacketListenerImpl serverConfigurationPacketListener);

    /**
     * Sends packet to server
     * @param packet Network packet
     * @param stage Packet type (only used in fabric implementation)
     */
    void sendPacketToServer(CustomPacketPayload packet, PacketStageType stage);
}
