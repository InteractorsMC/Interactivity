/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.client.physics.world;

import net.minecraft.client.multiplayer.ClientLevel;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;
import org.clwn.interactivity.api.util.Side;

public interface ClientPhysicsWorld extends PhysicsWorld {
    ClientLevel getClientLevel();

    @Override
    default Side getSide() {
        return Side.CLIENT;
    }
}
