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
 * Writes the custom state of a body, read back by {@link BodyDataReader}.
 * Runs on the physics thread, so the state it reads has to be safe to read from there (e.g. volatile fields).
 */
@FunctionalInterface
public interface BodyDataWriter {
    void write(Body body, FriendlyByteBuf buf);
}
