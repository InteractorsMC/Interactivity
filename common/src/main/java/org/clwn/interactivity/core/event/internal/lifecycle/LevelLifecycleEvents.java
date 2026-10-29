/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.event.internal.lifecycle;

import net.minecraft.server.level.ServerLevel;
import org.clwn.interactivity.core.event.internal.Event;

public class LevelLifecycleEvents {
    public static final Event<ServerLevelEvent> LEVEL_LOAD_EVENT = new Event<>();
    public static final Event<ServerLevelEvent> LEVEL_UNLOAD_EVENT = new Event<>();

    public record ServerLevelEvent(ServerLevel level) {
    }
}
