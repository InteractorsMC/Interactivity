/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.network;

import net.minecraft.world.entity.player.Player;
import org.clwn.interactivity.api.util.Side;

public record PacketContext(Player player, Side receiverSide) { }
