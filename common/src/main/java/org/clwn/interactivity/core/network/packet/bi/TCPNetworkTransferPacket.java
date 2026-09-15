/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.network.packet.bi;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.clwn.interactivity.api.network.NetworkPacket;
import org.clwn.interactivity.core.util.InteractivityIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper over Minecraft Networking packet to transfer Interactivity packets
 * @param identifier Registered packet identifier
 * @param packet Packet (only used for encoding)
 * @param buf Remaining data buffer (only used for handling in network manager)
 */
public record TCPNetworkTransferPacket(ResourceLocation identifier,
                                       @Nullable NetworkPacket packet,
                                       @Nullable FriendlyByteBuf buf) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TCPNetworkTransferPacket> TYPE = new CustomPacketPayload.Type<>(InteractivityIdentifier.getIdentifier("tcp_data"));

    public static final StreamCodec<FriendlyByteBuf, TCPNetworkTransferPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull TCPNetworkTransferPacket decode(@NotNull FriendlyByteBuf buf) {
            return new TCPNetworkTransferPacket(
                    buf.readResourceLocation(),
                    null,
                    buf
            );
        }

        @Override
        public void encode(FriendlyByteBuf buf, TCPNetworkTransferPacket packet) {
            buf.writeResourceLocation(packet.identifier);
            assert packet.packet != null;
            packet.packet.encode(buf);
        }
    };

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}