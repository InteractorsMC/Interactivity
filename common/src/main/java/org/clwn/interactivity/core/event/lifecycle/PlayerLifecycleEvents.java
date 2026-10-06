/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.event.lifecycle;

import net.minecraft.world.entity.player.Player;
import org.clwn.interactivity.core.event.Event;

public class PlayerLifecycleEvents {
    public static final Event<PlayerConfigureStateEvent> PLAYER_CONFIGURE_STATE_EVENT = new Event<>();

    // todo: actually configure something and add listeners
    public record PlayerConfigureStateEvent(Player player) {
    }

}
