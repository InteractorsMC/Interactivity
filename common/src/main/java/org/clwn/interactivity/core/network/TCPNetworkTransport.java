/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.function.TriConsumer;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.api.network.NetworkPacket;
import org.clwn.interactivity.api.network.NetworkTransport;
import org.clwn.interactivity.api.network.PacketContext;
import org.clwn.interactivity.core.config.InteractivityConfig;
import org.clwn.interactivity.core.network.packet.bi.TCPNetworkTransferPacket;
import org.clwn.interactivity.core.util.platform.Services;
import org.clwn.interactivity.core.util.platform.services.INetworkingHelper;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class TCPNetworkTransport implements NetworkTransport {
    private static @Nullable TriConsumer<PacketContext, ResourceLocation, FriendlyByteBuf> currentHandler;
    private static boolean isInitialized = false;

    public TCPNetworkTransport() {
        if (!isInitialized) {
            Services.PLATFORM_NETWORK.registerPacket(
                    INetworkingHelper.PacketStageType.PLAY,
                    INetworkingHelper.PacketType.BIDIRECTIONAL,
                    TCPNetworkTransferPacket.TYPE,
                    TCPNetworkTransferPacket.STREAM_CODEC,
                    ((context, packet) -> {
                        if (currentHandler != null) {
                            currentHandler.accept(context, packet.identifier(), packet.buf());
                        }
                    })
            );
            isInitialized = true;
        }
    }

    @Override
    public void startListening(TriConsumer<PacketContext, ResourceLocation, FriendlyByteBuf> packetHandler) {
        if (currentHandler != null) {
            Interactivity.LOGGER.warn("TCP transport is already listening. Overriding handler, expect things to break.");
        }

        currentHandler = packetHandler;
    }

    @Override
    public void stopListening() {
        currentHandler = null;
    }

    @Override
    public void initializeConnection(Player player, Consumer<Boolean> onComplete) {

    }

    @Override
    public boolean canBeUsedForPlayer(Player player) {
        return true;
    }

    @Override
    public void sendPacketToPlayer(ServerPlayer player, NetworkPacket packet) {
        Services.PLATFORM_NETWORK.sendPacketToPlayer(
                player,
                new TCPNetworkTransferPacket(packet.getIdentifier(), packet, null),
                INetworkingHelper.PacketStageType.PLAY,
                null
        );
    }

    @Override
    public void sendPacketToServer(NetworkPacket packet) {
        Services.PLATFORM_NETWORK.sendPacketToServer(
                new TCPNetworkTransferPacket(packet.getIdentifier(), packet, null),
                INetworkingHelper.PacketStageType.PLAY
        );
    }

    @Override
    public int getPriority() {
        return InteractivityConfig.tcpTransport;
    }
}
