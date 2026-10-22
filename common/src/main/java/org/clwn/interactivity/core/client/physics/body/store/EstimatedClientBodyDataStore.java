/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.client.physics.body.store;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import lombok.Getter;
import org.clwn.interactivity.core.physics.body.store.AbstractBodyDataStore;
import org.clwn.interactivity.core.physics.body.store.HistoricalPhysicsStateContainer;
import org.clwn.interactivity.core.physics.body.store.PhysicsStateContainer;

/**
 * Body store of a non-simulated client world.
 * <p>
 * The live container always holds the latest state received from the server. The history container
 * (size 1) holds the previous network state of each body, written by the body manager right before a new state is applied.
 * Only accessed from the client main thread.
 */
public class EstimatedClientBodyDataStore extends AbstractBodyDataStore {
    /**
     * Number of previous network states kept per body. One is enough to interpolate between two states.
     */
    public static final int HISTORY_SIZE = 1;

    @Getter
    protected PhysicsStateContainer physicsStateContainer;

    @Getter
    protected HistoricalPhysicsStateContainer history;

    /**
     * Maps a server-assigned network id to the index in the arrays.
     */
    @Getter
    protected final Int2IntMap networkIdToIndex = new Int2IntOpenHashMap();

    public EstimatedClientBodyDataStore() {
        super();
        networkIdToIndex.defaultReturnValue(-1);
    }

    public int getIndexByNetworkId(int networkId) {
        return networkIdToIndex.get(networkId);
    }

    @Override
    protected void allocate(int newCapacity) {
        PhysicsStateContainer newContainer = new PhysicsStateContainer(newCapacity);

        if (physicsStateContainer != null) {
            physicsStateContainer.copyTo(newContainer);
        }

        if (history == null) {
            history = new HistoricalPhysicsStateContainer(HISTORY_SIZE, newCapacity);
        } else {
            history.allocate(newCapacity);
        }

        this.physicsStateContainer = newContainer;
        this.capacity = newCapacity;
    }

    @Override
    public void clear() {
        super.clear();
        networkIdToIndex.clear();
    }
}
