/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.body;

import lombok.Getter;
import org.clwn.interactivity.api.physics.body.AddReason;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.RemovalReason;
import org.clwn.interactivity.core.physics.body.store.ServerBodyDataStore;
import org.clwn.interactivity.core.physics.body.store.SimulatedPhysicsStateContainer;
import org.clwn.interactivity.core.physics.network.NetworkIdAllocator;
import org.clwn.interactivity.core.physics.world.ServerPhysicsWorldImpl;

public class ServerBodyManager extends SimulatedBodyManager {
    @Getter
    protected final ServerBodyDataStore bodyDataStore = new ServerBodyDataStore();

    @Getter
    private final BodyChunkTracker chunkTracker = new BodyChunkTracker();

    private final ServerPhysicsWorldImpl serverWorld;

    public ServerBodyManager(ServerPhysicsWorldImpl physicsWorld) {
        super(physicsWorld);
        this.serverWorld = physicsWorld;
    }

    @Override
    protected void onBodyAdd(Body body, int index, AddReason addReason) {
        super.onBodyAdd(body, index, addReason);

        SimulatedPhysicsStateContainer container = bodyDataStore.getPhysicsStateContainer();

        container.networkId.put(index, NetworkIdAllocator.next());
        chunkTracker.onBodyAdded(index, container);
    }

    @Override
    protected void onBodyRemove(Body body, int index, RemovalReason removalReason) {
        super.onBodyRemove(body, index, removalReason);

        SimulatedPhysicsStateContainer container = bodyDataStore.getPhysicsStateContainer();

        chunkTracker.onBodyRemoved(index, container);
        serverWorld.getSaveManager().onBodyRemoved(body, removalReason);
    }
}
