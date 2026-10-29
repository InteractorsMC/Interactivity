/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.event.internal.lifecycle;

import net.minecraft.server.MinecraftServer;
import org.clwn.interactivity.core.event.internal.Event;

public final class ServerLifecycleEvents {
    public static final Event<ServerEvent> SERVER_STARTING_EVENT = new Event<>();
    public static final Event<ServerEvent> SERVER_STOPPING_EVENT = new Event<>();

    public record ServerEvent(MinecraftServer server) {

    }
}
