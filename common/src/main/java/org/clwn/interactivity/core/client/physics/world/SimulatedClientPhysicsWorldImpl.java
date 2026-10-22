/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.client.physics.world;

import lombok.Getter;
import net.minecraft.client.multiplayer.ClientLevel;
import org.clwn.interactivity.api.client.physics.world.SimulatedClientPhysicsWorld;
import org.clwn.interactivity.core.client.physics.ClientPhysicsClock;
import org.clwn.interactivity.core.client.physics.body.SimulatedClientBodyManager;
import org.clwn.interactivity.core.physics.world.PhysicsSimulationConfig;
import org.clwn.interactivity.core.physics.world.SimulatedPhysicsWorldImpl;

public class SimulatedClientPhysicsWorldImpl extends SimulatedPhysicsWorldImpl implements SimulatedClientPhysicsWorld, ClientPhysicsWorldInternal {
    @Getter
    private final ClientLevel clientLevel;

    public SimulatedClientPhysicsWorldImpl(ClientLevel level, PhysicsSimulationConfig physicsSimulationConfig) {
        super(level, physicsSimulationConfig);
        this.clientLevel = level;
        throw new UnsupportedOperationException("The simulated client world is not implemented yet");
    }

    @Override
    public SimulatedClientBodyManager getBodyManager() {
        // todo
        return null;
    }

    @Override
    public void onServerTick(int serverTick) {

    }

    @Override
    public ClientPhysicsClock getClock() {
        return null;
    }
}
