/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.body;

import lombok.Getter;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.core.physics.body.store.ServerBodyDataStore;
import org.clwn.interactivity.core.physics.body.store.SimulatedPhysicsStateContainer;
import org.clwn.interactivity.core.physics.network.NetworkDispatcher;
import org.clwn.interactivity.core.physics.network.NetworkIdAllocator;
import org.clwn.interactivity.core.physics.world.ServerPhysicsWorldImpl;

public class ServerBodyManager extends SimulatedBodyManager {
    @Getter
    protected final ServerBodyDataStore bodyDataStore = new ServerBodyDataStore();

    private final ServerPhysicsWorldImpl serverWorld;

    public ServerBodyManager(ServerPhysicsWorldImpl physicsWorld) {
        super(physicsWorld);
        this.serverWorld = physicsWorld;
    }

    @Override
    protected void onBodyAdd(Body body, int index) {
        SimulatedPhysicsStateContainer container = bodyDataStore.getPhysicsStateContainer();

        int networkId = NetworkIdAllocator.next();
        long chunkKey = NetworkDispatcher.chunkKeyOf(container.posX.get(index), container.posZ.get(index));

        container.networkId.put(index, networkId);
        container.chunkKey.put(index, chunkKey);

        serverWorld.getNetworkDispatcher().onBodyAdded(networkId, chunkKey, index);
    }

    @Override
    protected void onBodyRemove(Body body, int index) {
        SimulatedPhysicsStateContainer container = bodyDataStore.getPhysicsStateContainer();

        serverWorld.getNetworkDispatcher().onBodyRemove(container.networkId.get(index), container.chunkKey.get(index));
    }
}
