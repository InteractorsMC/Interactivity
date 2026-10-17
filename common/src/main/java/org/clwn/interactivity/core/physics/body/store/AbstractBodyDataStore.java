/*
 * Copyright (c) 2025-2026 xI-Mx-Ix
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.body.store;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.PhysicsStateAccessor;
import org.clwn.interactivity.api.util.math.Quat;
import org.clwn.interactivity.api.util.math.Vec3d;
import org.clwn.interactivity.api.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class AbstractBodyDataStore implements PhysicsStateAccessor {
    /**
     * The default initial capacity of the data arrays before any growth occurs.
     */
    protected static final int INITIAL_CAPACITY = 1024;

    /**
     * Maps a persistent Body UUID to its current index in the SoA arrays.
     */
    protected final Object2IntMap<UUID> uuidToIndex = new Object2IntOpenHashMap<>();

    /**
     * Reverse mapping from index to UUID.
     */
    protected final ObjectArrayList<UUID> indexToUuid = new ObjectArrayList<>();

    /**
     * Stack of indices that were previously removed and can be reused to keep arrays compact.
     */
    protected final IntArrayList freeIndices = new IntArrayList();

    /**
     * The number of active bodies.
     * Note: This count may include holes if accessed raw, but `uuidToIndex.size()` is the true active count.
     */
    protected int count = 0;

    /**
     * The current allocated size of the arrays.
     */
    protected int capacity = 0;

    public abstract PhysicsStateContainer getPhysicsStateContainer();

    public AbstractBodyDataStore() {
        uuidToIndex.defaultReturnValue(-1);
        allocate(INITIAL_CAPACITY);
    }

    public int getIndexByUuid(UUID uuid) {
        return uuidToIndex.getInt(uuid);
    }

    public UUID getUuidByIndex(int index) {
        if (index < 0 || index >= indexToUuid.size()) {
            return null;
        }
        return indexToUuid.get(index);
    }

    /**
     * Reserves a new index for the specified body object.
     * <p>
     * If the arrays are full, this method triggers a {@link #allocate(int)} call to grow them.
     * This method automatically populates the {@code bodies} reference array at the reserved index.
     *
     * @param body The body instance to add to the store.
     * @return The assigned data store index.
     */
    public int addBody(Body body) {
        if (count >= capacity) {
            allocate(capacity * 2);
            // todo: add history container and store data inside using TICK % historySize (0 for server, 1 for no-prediction client, N for prediction client)
            // this shouldn't use much memory since the data is mostly primitive types, but it will allow for rollback and prediction correction
            // also store jolt state in native allocated memory since it's not needed in java
            // if historySize is 0, history container should be either null or ignore any writes (need to decide later, but probably 2nd option)

        }

        // Reuse a freed index if available to keep arrays compact
        int index;
        if (!freeIndices.isEmpty()) {
            index = freeIndices.removeInt(freeIndices.size() - 1);
        } else {
            index = count++;
        }

        UUID id = body.getUuid();
        uuidToIndex.put(id, index);

        if (index >= indexToUuid.size()) {
            indexToUuid.add(id);
        } else {
            indexToUuid.set(index, id);
        }

        getPhysicsStateContainer().bodies[index] = body;

        return index;
    }

    /**
     * Removes the body mapping for the given UUID and marks the index as free.
     * <p>
     * This calls {@link #resetIndex(int)} to clear data at that index.
     *
     * @param id The UUID of the body to remove.
     * @return The index that was freed, or null if the body did not exist.
     */
    @Nullable
    public Integer removeBody(UUID id) {
        int index = uuidToIndex.removeInt(id);
        if (index != -1) {
            resetIndex(index);
            freeIndices.add(index);
            indexToUuid.set(index, null);
            return index;
        }
        return null;
    }

    /**
     * Resets all data fields at the specified index to their default values.
     * <p>
     * Delegates the clearing of physical state and object references to the
     * active {@link PhysicsStateContainer}.
     *
     * @param index The index to reset.
     */
    protected void resetIndex(int index) {
        getPhysicsStateContainer().reset(index);
    }

    /**
     * Allocates or re-allocates the underlying arrays to the specified capacity.
     *
     * @param newCapacity The new capacity for the data arrays.
     */
    protected abstract void allocate(int newCapacity);

    /**
     * Clears all mapped bodies and resets the store to its initial state.
     * <p>
     * Reallocates the default initial capacity to ensure that all data
     * is physically cleared and memory is reclaimed, while maintaining
     * the structural mapping capacity.
     */
    public void clear() {
        uuidToIndex.clear();
        indexToUuid.clear();
        freeIndices.clear();
        count = 0;

        // Allocation will create a fresh container, effectively clearing data.
        allocate(INITIAL_CAPACITY);
    }

    @Override
    public Vec3d getPosition(UUID bodyId) {
        return getPhysicsStateContainer().pos[uuidToIndex.getInt(bodyId)];
    }

    @Override
    public Quat getRotation(UUID bodyId) {
        return getPhysicsStateContainer().rot[uuidToIndex.getInt(bodyId)];
    }

    @Override
    public Vec3f getLinearVelocity(UUID bodyId) {
        return getPhysicsStateContainer().vel[uuidToIndex.getInt(bodyId)];
    }

    @Override
    public Vec3f getAngularVelocity(UUID bodyId) {
        return getPhysicsStateContainer().angVel[uuidToIndex.getInt(bodyId)];
    }

    @Override
    public boolean isActive(UUID bodyId) {
        return getPhysicsStateContainer().isActive.get(uuidToIndex.getInt(bodyId)) == 1;
    }

    @Override
    public long getBehaviorFlags(UUID bodyId) {
        return getPhysicsStateContainer().behaviorFlags.get(uuidToIndex.getInt(bodyId));
    }
}
