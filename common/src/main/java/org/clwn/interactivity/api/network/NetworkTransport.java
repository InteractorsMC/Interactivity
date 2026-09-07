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
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;


/**
 * Abstract network transport interface.
 *
 * @implSpec Implementations must create separate networking thread,
 * if not using existing networking thread (e.g., Minecraft's networking thread).
 */
public interface NetworkTransport {
    /**
     * Starts listening for incoming packets and invokes the provided packet handler for each received packet.
     * @param packetHandler A consumer that will be invoked with the received packet buffer.
     */
    @ApiStatus.Internal
    void startListening(Consumer<FriendlyByteBuf> packetHandler);

    @ApiStatus.Internal
    void stopListening();

    /**
     * Initializes the network connection for the specified player.
     * This method is called when a player reaches configuration stage.
     *
     * @param player     The player for whom the network connection should be initialized.
     * @param onComplete A callback that will be invoked with {@code true} if the connection was successfully initialized,
     *                   or {@code false} otherwise.
     */
    void initializeConnection(Player player, Consumer<Boolean> onComplete);

    @ApiStatus.Internal
    void sendPacketToPlayer(NetworkPacket packet, Player player);

    @ApiStatus.Internal
    void sendPacketToServer(NetworkPacket packet);
}
