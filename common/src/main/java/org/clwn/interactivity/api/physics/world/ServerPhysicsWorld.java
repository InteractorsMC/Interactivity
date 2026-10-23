/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.world;

import net.minecraft.server.level.ServerLevel;
import org.clwn.interactivity.api.physics.body.BodyManager;
import org.clwn.interactivity.api.util.Side;

public interface ServerPhysicsWorld extends SimulatedPhysicsWorld {
    ServerLevel getServerLevel();
    BodyManager getBodyManager();

    @Override
    default Side getSide() {
        return Side.SERVER;
    }
}
