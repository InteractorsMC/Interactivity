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
import org.clwn.interactivity.core.util.InteractivityIdentifier;
import org.jetbrains.annotations.NotNull;

/**
 * Tells a client that bodies are gone.
 *
 * @param dimension  dimension the bodies were in
 * @param networkIds network ids of the removed bodies
 */
public record S2CBodyRemovePacket(ResourceKey<Level> dimension, int[] networkIds) implements CustomPacketPayload {
    public static final Type<S2CBodyRemovePacket> TYPE =
            new Type<>(InteractivityIdentifier.getIdentifier("body_remove"));

    public static final StreamCodec<FriendlyByteBuf, S2CBodyRemovePacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeResourceKey(packet.dimension);
                buf.writeVarIntArray(packet.networkIds);
            },
            buf -> new S2CBodyRemovePacket(buf.readResourceKey(Registries.DIMENSION), buf.readVarIntArray())
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
