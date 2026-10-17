/*
 * Copyright (c) 2025-2026 xI-Mx-Ix
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.body.store;

import lombok.Getter;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.util.math.Quat;
import org.clwn.interactivity.api.util.math.Vec3d;
import org.clwn.interactivity.api.util.math.Vec3f;
import org.clwn.interactivity.core.util.buffer.BufferUtils;

import java.nio.*;

public class PhysicsStateContainer {
    /**
     * The total pre-allocated capacity of the data arrays in this container.
     */
    @Getter
    public final int capacity;

    public final Vec3d[] pos;

    /**
     * X-coordinate of the body position in world space.
     */
    public final DoubleBuffer posX;
    /**
     * Y-coordinate of the body position in world space.
     */
    public final DoubleBuffer posY;
    /**
     * Z-coordinate of the body position in world space.
     */
    public final DoubleBuffer posZ;

    public final Quat[] rot;

    /**
     * X-component of the body rotation quaternion.
     */
    public final FloatBuffer rotX;
    /**
     * Y-component of the body rotation quaternion.
     */
    public final FloatBuffer rotY;
    /**
     * Z-component of the body rotation quaternion.
     */
    public final FloatBuffer rotZ;
    /**
     * W-component of the body rotation quaternion.
     */
    public final FloatBuffer rotW;

    public final Vec3f[] vel;

    /**
     * X-component of the linear velocity.
     */
    public final FloatBuffer velX;
    /**
     * Y-component of the linear velocity.
     */
    public final FloatBuffer velY;
    /**
     * Z-component of the linear velocity.
     */
    public final FloatBuffer velZ;

    public final Vec3f[] angVel;

    /**
     * X-component of the angular velocity (radians/sec).
     */
    public final FloatBuffer angVelX;
    /**
     * Y-component of the angular velocity (radians/sec).
     */
    public final FloatBuffer angVelY;
    /**
     * Z-component of the angular velocity (radians/sec).
     */
    public final FloatBuffer angVelZ;

    /**
     * Minimum X-coordinate of the world-space Axis-Aligned Bounding Box (AABB).
     */
    public final FloatBuffer aabbMinX;
    /**
     * Minimum Y-coordinate of the world-space Axis-Aligned Bounding Box (AABB).
     */
    public final FloatBuffer aabbMinY;
    /**
     * Minimum Z-coordinate of the world-space Axis-Aligned Bounding Box (AABB).
     */
    public final FloatBuffer aabbMinZ;

    /**
     * Maximum X-coordinate of the world-space Axis-Aligned Bounding Box (AABB).
     */
    public final FloatBuffer aabbMaxX;
    /**
     * Maximum Y-coordinate of the world-space Axis-Aligned Bounding Box (AABB).
     */
    public final FloatBuffer aabbMaxY;
    /**
     * Maximum Z-coordinate of the world-space Axis-Aligned Bounding Box (AABB).
     */
    public final FloatBuffer aabbMaxZ;

    /**
     * Whether the body is currently active and ticking in the physics simulation.
     */
    public final ByteBuffer isActive;
    /**
     * Bitmask representing attached behaviors and their network/tick states.
     */
    public final LongBuffer behaviorFlags;

    /**
     * Reference to the high-level {@link Body} wrapper objects for each slot.
     */
    public final Body[] bodies;

    public PhysicsStateContainer(int capacity) {
        this.capacity = capacity;

        this.pos = new Vec3d[capacity];
        this.posX = ByteBuffer.allocateDirect(capacity * Double.BYTES).order(ByteOrder.nativeOrder()).asDoubleBuffer();
        this.posY = ByteBuffer.allocateDirect(capacity * Double.BYTES).order(ByteOrder.nativeOrder()).asDoubleBuffer();
        this.posZ = ByteBuffer.allocateDirect(capacity * Double.BYTES).order(ByteOrder.nativeOrder()).asDoubleBuffer();

        for (int i = 0; i < capacity; i++) {
            this.pos[i] = new Vec3dView(this.posX, this.posY, this.posZ, i);
        }

        this.rot = new Quat[capacity];
        this.rotX = ByteBuffer.allocateDirect(capacity * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.rotY = ByteBuffer.allocateDirect(capacity * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.rotZ = ByteBuffer.allocateDirect(capacity * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.rotW = ByteBuffer.allocateDirect(capacity * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();

        for (int i = 0; i < capacity; i++) {
            this.rot[i] = new QuatView(this.rotX, this.rotY, this.rotZ, this.rotW, i);
        }

        this.vel = new Vec3f[capacity];
        this.velX = ByteBuffer.allocateDirect(capacity * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.velY = ByteBuffer.allocateDirect(capacity * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.velZ = ByteBuffer.allocateDirect(capacity * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (int i = 0; i < capacity; i++) {
            this.vel[i] = new Vec3fView(this.velX, this.velY, this.velZ, i);
        }

        this.angVel = new Vec3f[capacity];
        this.angVelX = ByteBuffer.allocateDirect(capacity * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.angVelY = ByteBuffer.allocateDirect(capacity * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.angVelZ = ByteBuffer.allocateDirect(capacity * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (int i = 0; i < capacity; i++) {
            this.angVel[i] = new Vec3fView(this.angVelX, this.angVelY, this.angVelZ, i);
        }

        this.aabbMinX = ByteBuffer.allocateDirect(capacity * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.aabbMinY = ByteBuffer.allocateDirect(capacity * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.aabbMinZ = ByteBuffer.allocateDirect(capacity * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.aabbMaxX = ByteBuffer.allocateDirect(capacity * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.aabbMaxY = ByteBuffer.allocateDirect(capacity * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();

        this.aabbMaxZ = ByteBuffer.allocateDirect(capacity * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();

        this.isActive = ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());

        this.behaviorFlags = ByteBuffer.allocateDirect(capacity * Long.BYTES).order(ByteOrder.nativeOrder()).asLongBuffer();

        this.bodies = new Body[capacity];

    }

    /**
     * Copies all base physics data from this container to another container.
     * <p>
     * This is used during array resizing to migrate existing state to a larger
     * memory allocation while maintaining data integrity.
     *
     * @param other The destination container to copy data into.
     * @throws IllegalArgumentException if the other container's capacity is smaller than this container's.
     */
    public void copyTo(PhysicsStateContainer other) {
        int copyLength = Math.min(this.capacity, other.capacity);

        BufferUtils.copyDoubleBuffer(this.posX, other.posX, copyLength);
        BufferUtils.copyDoubleBuffer(this.posY, other.posY, copyLength);
        BufferUtils.copyDoubleBuffer(this.posZ, other.posZ, copyLength);

        BufferUtils.copyFloatBuffer(this.rotX, other.rotX, copyLength);
        BufferUtils.copyFloatBuffer(this.rotY, other.rotY, copyLength);
        BufferUtils.copyFloatBuffer(this.rotZ, other.rotZ, copyLength);
        BufferUtils.copyFloatBuffer(this.rotW, other.rotW, copyLength);

        BufferUtils.copyFloatBuffer(this.velX, other.velX, copyLength);
        BufferUtils.copyFloatBuffer(this.velY, other.velY, copyLength);
        BufferUtils.copyFloatBuffer(this.velZ, other.velZ, copyLength);

        BufferUtils.copyFloatBuffer(this.angVelX, other.angVelX, copyLength);
        BufferUtils.copyFloatBuffer(this.angVelY, other.angVelY, copyLength);
        BufferUtils.copyFloatBuffer(this.angVelZ, other.angVelZ, copyLength);
        BufferUtils.copyFloatBuffer(this.aabbMinX, other.aabbMinX, copyLength);
        BufferUtils.copyFloatBuffer(this.aabbMinY, other.aabbMinY, copyLength);
        BufferUtils.copyFloatBuffer(this.aabbMinZ, other.aabbMinZ, copyLength);
        BufferUtils.copyFloatBuffer(this.aabbMaxX, other.aabbMaxX, copyLength);
        BufferUtils.copyFloatBuffer(this.aabbMaxY, other.aabbMaxY, copyLength);
        BufferUtils.copyFloatBuffer(this.aabbMaxZ, other.aabbMaxZ, copyLength);

        BufferUtils.copyByteBuffer(this.isActive, other.isActive, copyLength);

        BufferUtils.copyLongBuffer(this.behaviorFlags, other.behaviorFlags, copyLength);

        System.arraycopy(this.bodies, 0, other.bodies, 0, copyLength);
    }

    /**
     * Resets all base physics data at the specified index to default values.
     * <p>
     * This clears object references to prevent memory leaks and restores
     * mathematical identity values (e.g., identity quaternion for rotation).
     *
     * @param index The slot index to clear.
     */
    public void reset(int index) {
        this.bodies[index] = null;

        this.posX.put(index, 0.0);
        this.posY.put(index, 0.0);
        this.posZ.put(index, 0.0);
        this.rotX.put(index, 0f);

        this.rotY.put(index, 0f);
        this.rotZ.put(index, 0f);
        this.rotW.put(index, 1f);

        this.velX.put(index, 0f);
        this.velY.put(index, 0f);
        this.velZ.put(index, 0f);

        this.angVelX.put(index, 0f);
        this.angVelY.put(index, 0f);
        this.angVelZ.put(index, 0f);

        this.aabbMinX.put(index, 0f);
        this.aabbMinY.put(index, 0f);
        this.aabbMinZ.put(index, 0f);
        this.aabbMaxX.put(index, 0f);
        this.aabbMaxY.put(index, 0f);
        this.aabbMaxZ.put(index, 0f);

        this.isActive.put(index, (byte) 0);

        this.behaviorFlags.put(index, 0L);
    }
}
