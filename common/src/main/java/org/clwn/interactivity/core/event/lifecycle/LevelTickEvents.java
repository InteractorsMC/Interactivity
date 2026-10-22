/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.event.lifecycle;

import net.minecraft.server.level.ServerLevel;
import org.clwn.interactivity.core.event.Event;

public class LevelTickEvents {
    public static final Event<ServerLevelTickEvent> END = new Event<>();

    public record ServerLevelTickEvent(ServerLevel level) {
    }
}
