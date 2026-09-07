/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface NetworkPacket {
    void encode(FriendlyByteBuf buf);

    /**
     * Handles the received network packet.
     *
     * @param player The player sending the packet, or {@code null} if the packet is sent by server.
     */
    void handlePacket(@Nullable Player player);
}
