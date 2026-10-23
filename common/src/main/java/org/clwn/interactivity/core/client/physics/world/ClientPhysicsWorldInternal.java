/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.client.physics.world;

import net.minecraft.client.multiplayer.ClientLevel;
import org.clwn.interactivity.api.client.physics.world.ClientPhysicsWorld;
import org.clwn.interactivity.core.client.physics.ClientPhysicsClock;
import org.clwn.interactivity.core.client.physics.body.ClientBodyManager;
import org.clwn.interactivity.core.physics.body.AbstractBodyManager;

public interface ClientPhysicsWorldInternal extends ClientPhysicsWorld {
    ClientLevel getClientLevel();

    ClientPhysicsClock getClock();

    <T extends AbstractBodyManager & ClientBodyManager> T getBodyManager();

    void onServerTick(int serverTick);
}
