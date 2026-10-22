/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.network;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hands out the compact ids bodies are referred to by in state packets.
 * <p>
 * Ids are never reused and are unique across all worlds of the server. A packet that arrives late
 * can therefore never be applied to a different body than the one it was meant for.
 */
public final class NetworkIdAllocator {
    private static final AtomicInteger NEXT = new AtomicInteger();

    private NetworkIdAllocator() {}

    public static int next() {
        return NEXT.getAndIncrement();
    }
}
