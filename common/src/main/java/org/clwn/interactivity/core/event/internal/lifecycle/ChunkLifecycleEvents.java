/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.event.internal.lifecycle;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.clwn.interactivity.core.event.internal.Event;

/**
 * Server chunk lifecycle. Both events are published on the server thread.
 */
public class ChunkLifecycleEvents {
    /**
     * A full chunk was added to the level. Its sections can be read at this point.
     */
    public static final Event<ServerChunkEvent> CHUNK_LOAD_EVENT = new Event<>();

    /**
     * A full chunk is about to be removed from the level. It is still readable during this event.
     */
    public static final Event<ServerChunkEvent> CHUNK_UNLOAD_EVENT = new Event<>();

    public record ServerChunkEvent(ServerLevel level, LevelChunk chunk) {
    }
}
