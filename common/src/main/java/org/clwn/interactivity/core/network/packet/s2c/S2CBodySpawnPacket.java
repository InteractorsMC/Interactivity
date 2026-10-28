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
import org.clwn.interactivity.core.physics.network.BodyStateCodec;
import org.clwn.interactivity.core.util.InteractivityIdentifier;
import org.jetbrains.annotations.NotNull;

/**
 * Tells a client that bodies appeared in its level.
 *
 * @param dimension dimension the bodies are in; the client ignores the packet if its level is another one
 * @param tick      physics tick the states belong to
 * @param count     number of entries in {@code data}
 * @param data      per entry: UUID, body type id, then the state (see {@link BodyStateCodec}),
 *                  then the data (length varint + bytes, see {@link })
 */
public record S2CBodySpawnPacket(ResourceKey<Level> dimension, int tick, int count, byte[] data) implements CustomPacketPayload {
    public static final Type<S2CBodySpawnPacket> TYPE =
            new Type<>(InteractivityIdentifier.getIdentifier("body_spawn"));

    public static final StreamCodec<FriendlyByteBuf, S2CBodySpawnPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeResourceKey(packet.dimension);
                buf.writeVarInt(packet.tick);
                buf.writeVarInt(packet.count);
                buf.writeBytes(packet.data);
            },
            buf -> {
                ResourceKey<Level> dimension = buf.readResourceKey(Registries.DIMENSION);
                int tick = buf.readVarInt();
                int count = buf.readVarInt();
                byte[] data = new byte[buf.readableBytes()];
                buf.readBytes(data);
                return new S2CBodySpawnPacket(dimension, tick, count, data);
            }
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
