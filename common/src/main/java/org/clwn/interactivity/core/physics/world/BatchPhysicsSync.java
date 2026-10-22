/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.world;

import org.clwn.interactivity.core.physics.body.store.SimulatedBodyDataStore;
import org.clwn.interactivity.core.physics.body.store.SimulatedPhysicsStateContainer;
import org.clwn.interactivity.natives.PhysicsSyncNative;

/**
 * Copies the state of all rigid bodies from Jolt into the data store with a single native call
 * and keeps the list of bodies that changed.
 */
public class BatchPhysicsSync {
    private final long physicsSystemVa;
    private final SimulatedBodyDataStore dataStore;

    /**
     * @param physicsSystemVa native address of the Jolt physics system
     * @param dataStore       the store to write the state into
     */
    public BatchPhysicsSync(long physicsSystemVa, SimulatedBodyDataStore dataStore) {
        this.physicsSystemVa = physicsSystemVa;
        this.dataStore = dataStore;
    }

    /**
     * Reads the state of every awake body (and every body that just fell asleep) from Jolt.
     * Bodies that changed are appended to the dirty list.
     */
    public void sync() {
        // The container is replaced when the store grows, so it must be fetched on every call
        SimulatedPhysicsStateContainer container = dataStore.getPhysicsStateContainer();

        int dirtyCount = PhysicsSyncNative.syncRigidBodies(
                physicsSystemVa,
                dataStore.getSlotCount(),
                container.joltBodyId,
                container.posX, container.posY, container.posZ,
                container.rotX, container.rotY, container.rotZ, container.rotW,
                container.velX, container.velY, container.velZ,
                container.angVelX, container.angVelY, container.angVelZ,
                container.aabbMinX, container.aabbMinY, container.aabbMinZ,
                container.aabbMaxX, container.aabbMaxY, container.aabbMaxZ,
                container.isActive,
                container.isDirty,
                container.dirtyIndices,
                container.getDirtyCount()
        );

        container.setDirtyCount(dirtyCount);
    }

    /**
     * Clears the dirty list and its markers. Called after everything that consumes the list has run.
     */
    public void clear() {
        SimulatedPhysicsStateContainer container = dataStore.getPhysicsStateContainer();

        int count = container.getDirtyCount();
        for (int k = 0; k < count; k++) {
            container.isDirty.put(container.dirtyIndices.get(k), (byte) 0);
        }
        container.setDirtyCount(0);
    }
}
