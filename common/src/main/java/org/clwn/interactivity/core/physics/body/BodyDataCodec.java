/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.body;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.api.physics.behavior.DataSyncBehavior;
import org.clwn.interactivity.api.physics.body.Body;

import java.util.Optional;

/**
 * Runs the {@link DataSyncBehavior} of a body. The same bytes are sent to clients and saved.
 * A failing writer or reader is logged and treated as no data, so it never breaks a packet or a save.
 */
public final class BodyDataCodec {
    private static final byte[] NO_DATA = new byte[0];

    private BodyDataCodec() {
    }

    /**
     * @return the data of the body, empty if its type has no {@link DataSyncBehavior}
     */
    public static byte[] write(Body body) {
        Optional<DataSyncBehavior> behavior = body.getType().getBehavior(DataSyncBehavior.class);
        if (behavior.isEmpty()) {
            return NO_DATA;
        }

        try {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            behavior.get().writer().write(body, buf);

            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            return data;
        } catch (Exception e) {
            Interactivity.LOGGER.error("Failed to write the data of body {} of type {}", body.getUuid(), body.getType().getTypeId(), e);
            return NO_DATA;
        }
    }

    /**
     * Applies data written by {@link #write}. Empty data is ignored.
     */
    public static void read(Body body, byte[] data) {
        Optional<DataSyncBehavior> behavior = body.getType().getBehavior(DataSyncBehavior.class);
        if (behavior.isEmpty() || data.length == 0) {
            return;
        }

        try {
            behavior.get().reader().read(body, new FriendlyByteBuf(Unpooled.wrappedBuffer(data)));
        } catch (Exception e) {
            Interactivity.LOGGER.error("Failed to read the data of body {} of type {}", body.getUuid(), body.getType().getTypeId(), e);
        }
    }
}
