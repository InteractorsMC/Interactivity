/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.body;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Reads the custom state written by {@link BodyDataWriter}, before the body is added to the world or when it changed.
 */
@FunctionalInterface
public interface BodyDataReader {
    void read(Body body, FriendlyByteBuf buf);
}
