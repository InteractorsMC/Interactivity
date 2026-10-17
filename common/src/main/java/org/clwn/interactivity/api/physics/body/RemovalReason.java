/*
 * Copyright (c) 2025-2026 xI-Mx-Ix
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.body;

public enum RemovalReason {
    /**
     *  The body is discarded and should not be saved.
     */
    DISCARD,

    /**
     * The body is being removed from memory because its chunk is unloading. Persistence is handled separately.
     */
    UNLOAD
}
