/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.network.packet.s2c;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.clwn.interactivity.api.physics.behavior.DataSyncBehavior;
import org.clwn.interactivity.core.util.InteractivityIdentifier;
import org.jetbrains.annotations.NotNull;

/**
 * Tells a client that the data of bodies it already has changed.
 *
 * @param dimension dimension the bodies are in; the client ignores the packet if its level is another one
 * @param count     number of entries in {@code data}
 * @param data      per entry: network id (varint), then the data (length varint + bytes, see {@link DataSyncBehavior})
 */
public record S2CBodyDataPacket(ResourceKey<Level> dimension, int count, byte[] data) implements CustomPacketPayload {
    public static final Type<S2CBodyDataPacket> TYPE =
            new Type<>(InteractivityIdentifier.getIdentifier("body_data"));

    public static final StreamCodec<FriendlyByteBuf, S2CBodyDataPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeResourceKey(packet.dimension);
                buf.writeVarInt(packet.count);
                buf.writeBytes(packet.data);
            },
            buf -> {
                ResourceKey<Level> dimension = buf.readResourceKey(Registries.DIMENSION);
                int count = buf.readVarInt();
                byte[] data = new byte[buf.readableBytes()];
                buf.readBytes(data);
                return new S2CBodyDataPacket(dimension, count, data);
            }
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
