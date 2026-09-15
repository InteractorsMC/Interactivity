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
import org.jetbrains.annotations.Nullable;

public interface NetworkPacket {
    int BYTEBUFFER_CAPACITY = 1350;

    ResourceLocation getIdentifier();

    /**
     * Encodes packet data into bytebuffer.
     * @param buf Bytebuffer
     * @implSpec Maximum amount of bytes that can be written is {@code BYTEBUFFER_CAPACITY}
     */
    void encode(FriendlyByteBuf buf);

    /**
     * Handles the received network packet.
     *
     * @param context The packet context
     */
    void handle(@Nullable PacketContext context);
}
