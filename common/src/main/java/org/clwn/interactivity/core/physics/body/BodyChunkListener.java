/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.body;

public interface BodyChunkListener {
    default void onBodyAdded(int index, long chunkKey) {
    }

    /**
     * A body is about to be removed from the world.
     */
    default void onBodyRemoved(int index, long chunkKey) {
    }

    /**
     * A body crossed a chunk border during the last physics tick.
     */
    default void onBodyMoved(int index, long fromChunkKey, long toChunkKey) {
    }
}
