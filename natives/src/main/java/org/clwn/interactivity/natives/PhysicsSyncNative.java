/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.natives;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * JNI binding for the batch Jolt to Java state synchronization.
 * <p>
 * All buffers must be direct and use native byte order. The call must be made from the
 * physics thread, after {@code PhysicsSystem#update} has returned and before any body is added or removed.
 */
public final class PhysicsSyncNative {
    private PhysicsSyncNative() {}

    /**
     * Copies the state of every active (or just deactivated, or already dirty) rigid body from Jolt into the buffers
     * and appends newly dirty slot indices to {@code dirtyIndices}.
     * <p>
     * A slot is skipped when its {@code joltBodyId} is {@code 0xFFFFFFFF} (free slot), or when the body is asleep,
     * was already asleep in the store, and is not already marked dirty.
     *
     * @param physicsSystemVa native address of the {@code JPH::PhysicsSystem}
     * @param slotCount       number of slots to scan
     * @param dirtyCount      number of entries already present in {@code dirtyIndices}
     * @return the new number of entries in {@code dirtyIndices}
     */
    public static native int syncRigidBodies(
            long physicsSystemVa,
            int slotCount,
            IntBuffer joltBodyId,
            DoubleBuffer posX, DoubleBuffer posY, DoubleBuffer posZ,
            FloatBuffer rotX, FloatBuffer rotY, FloatBuffer rotZ, FloatBuffer rotW,
            FloatBuffer velX, FloatBuffer velY, FloatBuffer velZ,
            FloatBuffer angVelX, FloatBuffer angVelY, FloatBuffer angVelZ,
            FloatBuffer aabbMinX, FloatBuffer aabbMinY, FloatBuffer aabbMinZ,
            FloatBuffer aabbMaxX, FloatBuffer aabbMaxY, FloatBuffer aabbMaxZ,
            ByteBuffer isActive,
            ByteBuffer isDirty,
            IntBuffer dirtyIndices,
            int dirtyCount
    );
}
