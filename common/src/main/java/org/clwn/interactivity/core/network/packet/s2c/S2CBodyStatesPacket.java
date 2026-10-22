/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.clwn.interactivity.api.network.NetworkPacket;
import org.clwn.interactivity.api.network.PacketContext;
import org.clwn.interactivity.core.client.network.ClientBodySyncHandler;
import org.clwn.interactivity.core.util.InteractivityIdentifier;
import org.jetbrains.annotations.Nullable;

public class S2CBodyStatesPacket implements NetworkPacket {
    public static final ResourceLocation IDENTIFIER = InteractivityIdentifier.getIdentifier("body_states");

    /**
     * Upper bound for the encoded records of one packet. Together with the headers of the packet and of the
     * transport it stays below {@link NetworkPacket#BYTEBUFFER_CAPACITY}.
     */
    public static final int MAX_DATA_BYTES = 1200;

    private final int tick;
    private final int count;
    private final byte[] data;
    private final int length;

    /**
     * @param tick   physics tick the states belong to
     * @param count  number of records in {@code data}
     * @param data   the records, see {@link org.clwn.interactivity.core.physics.network.BodyStateCodec}
     * @param length number of valid bytes in {@code data}
     */
    public S2CBodyStatesPacket(int tick, int count, byte[] data, int length) {
        this.tick = tick;
        this.count = count;
        this.data = data;
        this.length = length;
    }

    public static S2CBodyStatesPacket decode(FriendlyByteBuf buf) {
        int tick = buf.readVarInt();
        int count = buf.readVarInt();
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);

        return new S2CBodyStatesPacket(tick, count, data, data.length);
    }

    @Override
    public ResourceLocation getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(tick);
        buf.writeVarInt(count);
        buf.writeBytes(data, 0, length);
    }

    @Override
    public void handle(@Nullable PacketContext context) {
        ClientBodySyncHandler.handleStates(tick, count, data, length);
    }
}
