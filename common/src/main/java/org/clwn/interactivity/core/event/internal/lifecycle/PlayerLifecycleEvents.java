/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.event.internal.lifecycle;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.clwn.interactivity.core.event.internal.Event;

public class PlayerLifecycleEvents {
    public static final Event<PlayerConfigureStateEvent> PLAYER_CONFIGURE_STATE_EVENT = new Event<>();

    public static final Event<PlayerJoinEvent> PLAYER_JOIN_EVENT = new Event<>();

    // todo: actually configure something and add listeners
    public record PlayerConfigureStateEvent(Player player) {
    }

    public record PlayerJoinEvent(ServerPlayer player) {
    }

}
