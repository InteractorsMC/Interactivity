/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.network.packet;


import org.clwn.interactivity.api.InteractivityAPI;
import org.clwn.interactivity.core.client.network.ClientBodySyncHandler;
import org.clwn.interactivity.core.network.packet.s2c.S2CBodyRemovePacket;
import org.clwn.interactivity.core.network.packet.s2c.S2CBodySpawnPacket;
import org.clwn.interactivity.core.network.packet.s2c.S2CBodyStatesPacket;
import org.clwn.interactivity.core.network.packet.s2c.S2CPhysicsConfigPacket;
import org.clwn.interactivity.core.util.platform.Services;
import org.clwn.interactivity.core.util.platform.services.INetworkingHelper;

public class PacketRegistry {
    public static void registerPackets() {
        Services.PLATFORM_NETWORK.registerPacket(
                INetworkingHelper.PacketStageType.PLAY,
                INetworkingHelper.PacketType.S2C,
                S2CPhysicsConfigPacket.TYPE,
                S2CPhysicsConfigPacket.STREAM_CODEC,
                (context, packet) -> ClientBodySyncHandler.handleConfig(packet)
        );

        Services.PLATFORM_NETWORK.registerPacket(
                INetworkingHelper.PacketStageType.PLAY,
                INetworkingHelper.PacketType.S2C,
                S2CBodySpawnPacket.TYPE,
                S2CBodySpawnPacket.STREAM_CODEC,
                (context, packet) -> ClientBodySyncHandler.handleSpawn(packet)
        );

        Services.PLATFORM_NETWORK.registerPacket(
                INetworkingHelper.PacketStageType.PLAY,
                INetworkingHelper.PacketType.S2C,
                S2CBodyRemovePacket.TYPE,
                S2CBodyRemovePacket.STREAM_CODEC,
                (context, packet) -> ClientBodySyncHandler.handleRemove(packet)
        );

        InteractivityAPI.getInstance().getNetworkManager().registerS2C(S2CBodyStatesPacket.IDENTIFIER, S2CBodyStatesPacket::decode);
    }
}
