/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.mixin.event.lifecycle;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.clwn.interactivity.core.network.packet.s2c.HelloClientPacket;
import org.clwn.interactivity.core.util.platform.Services;
import org.clwn.interactivity.core.util.platform.services.INetworkingHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.charset.StandardCharsets;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    
    @Inject(at = @At("TAIL"), method = "placeNewPlayer")
    private void init(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo info) {
        Services.PLATFORM_NETWORK.sendPacketToPlayer(player, new HelloClientPacket("Hello from Interactivity!".getBytes(StandardCharsets.UTF_8)),  INetworkingHelper.PacketStageType.PLAY, null);
    }
}