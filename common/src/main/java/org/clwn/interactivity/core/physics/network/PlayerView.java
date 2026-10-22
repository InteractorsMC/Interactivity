/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.network;

import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Immutable copy of the data of a player the physics thread needs. Built by the server main thread.
 */
public record PlayerView(UUID uuid, ServerPlayer player, ChunkTrackingView view) {
}
