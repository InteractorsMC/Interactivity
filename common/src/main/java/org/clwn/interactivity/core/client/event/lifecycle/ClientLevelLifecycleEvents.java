/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.client.event.lifecycle;

import net.minecraft.client.multiplayer.ClientLevel;
import org.clwn.interactivity.core.event.Event;

public class ClientLevelLifecycleEvents {
    public static final Event<ClientLevelEvent> LEVEL_LOAD_EVENT = new Event<>();

    public static final Event<ClientLevelEvent> LEVEL_UNLOAD_EVENT = new Event<>();

    public record ClientLevelEvent(ClientLevel level) {
    }
}
