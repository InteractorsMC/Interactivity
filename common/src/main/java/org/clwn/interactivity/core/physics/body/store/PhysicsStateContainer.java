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
     * Network id of the body in each slot, {@code -1} when the slot is not synchronized.
     */
    public final IntBuffer networkId;

    /**
     * Physics tick the state in this slot belongs to.
     */
    public final IntBuffer stateTick;

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

        this.networkId = ByteBuffer.allocateDirect(capacity * Integer.BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();
        this.stateTick = ByteBuffer.allocateDirect(capacity * Integer.BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();

        for (int i = 0; i < capacity; i++) {
            this.rotW.put(i, 1f);
            this.networkId.put(i, -1);
        }

        this.bodies = new Body[capacity];

    }

    /**
     * Copies all base physics data from this container to another container.
     * <p>
     * This is used during array resizing to migrate existing state to a larger
     * memory allocation while maintaining data integrity.
     *
     * @param other The destination container to copy data into.
     */
    public void copyTo(PhysicsStateContainer other) {
        copyRangeTo(0, other, 0, Math.min(this.capacity, other.capacity));
    }

    /**
     * Copies the base physics data of a range of slots into a range of slots of another container.
     * <p>
     * Used by history containers, which keep several ticks in one container and have to move
     * each tick to its new position when the capacity changes.
     *
     * @param srcOffset Index of the first slot to read.
     * @param other     The destination container.
     * @param dstOffset Index of the first slot to write.
     * @param length    The number of slots to copy.
     */
    public void copyRangeTo(int srcOffset, PhysicsStateContainer other, int dstOffset, int length) {
        BufferUtils.copyDoubleBuffer(this.posX, srcOffset, other.posX, dstOffset, length);
        BufferUtils.copyDoubleBuffer(this.posY, srcOffset, other.posY, dstOffset, length);
        BufferUtils.copyDoubleBuffer(this.posZ, srcOffset, other.posZ, dstOffset, length);

        BufferUtils.copyFloatBuffer(this.rotX, srcOffset, other.rotX, dstOffset, length);
        BufferUtils.copyFloatBuffer(this.rotY, srcOffset, other.rotY, dstOffset, length);
        BufferUtils.copyFloatBuffer(this.rotZ, srcOffset, other.rotZ, dstOffset, length);
        BufferUtils.copyFloatBuffer(this.rotW, srcOffset, other.rotW, dstOffset, length);

        BufferUtils.copyFloatBuffer(this.velX, srcOffset, other.velX, dstOffset, length);
        BufferUtils.copyFloatBuffer(this.velY, srcOffset, other.velY, dstOffset, length);
        BufferUtils.copyFloatBuffer(this.velZ, srcOffset, other.velZ, dstOffset, length);

        BufferUtils.copyFloatBuffer(this.angVelX, srcOffset, other.angVelX, dstOffset, length);
        BufferUtils.copyFloatBuffer(this.angVelY, srcOffset, other.angVelY, dstOffset, length);
        BufferUtils.copyFloatBuffer(this.angVelZ, srcOffset, other.angVelZ, dstOffset, length);
        BufferUtils.copyFloatBuffer(this.aabbMinX, srcOffset, other.aabbMinX, dstOffset, length);
        BufferUtils.copyFloatBuffer(this.aabbMinY, srcOffset, other.aabbMinY, dstOffset, length);
        BufferUtils.copyFloatBuffer(this.aabbMinZ, srcOffset, other.aabbMinZ, dstOffset, length);
        BufferUtils.copyFloatBuffer(this.aabbMaxX, srcOffset, other.aabbMaxX, dstOffset, length);
        BufferUtils.copyFloatBuffer(this.aabbMaxY, srcOffset, other.aabbMaxY, dstOffset, length);
        BufferUtils.copyFloatBuffer(this.aabbMaxZ, srcOffset, other.aabbMaxZ, dstOffset, length);

        BufferUtils.copyByteBuffer(this.isActive, srcOffset, other.isActive, dstOffset, length);

        BufferUtils.copyLongBuffer(this.behaviorFlags, srcOffset, other.behaviorFlags, dstOffset, length);

        BufferUtils.copyIntBuffer(this.networkId, srcOffset, other.networkId, dstOffset, length);
        BufferUtils.copyIntBuffer(this.stateTick, srcOffset, other.stateTick, dstOffset, length);

        System.arraycopy(this.bodies, srcOffset, other.bodies, dstOffset, length);
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

        this.networkId.put(index, -1);
        this.stateTick.put(index, 0);
    }

    /**
     * Copies the physics state of a single slot (transform, velocities, bounds, activity and tick)
     * into a slot of another container.
     * <p>
     * Body references, behavior flags and network ids are intentionally not copied: they describe the slot
     * owner and not the state, and history containers do not need them.
     *
     * @param srcIndex The slot to read from.
     * @param dst      The destination container.
     * @param dstIndex The slot to write to.
     */
    public void copyStateTo(int srcIndex, PhysicsStateContainer dst, int dstIndex) {
        dst.posX.put(dstIndex, this.posX.get(srcIndex));
        dst.posY.put(dstIndex, this.posY.get(srcIndex));
        dst.posZ.put(dstIndex, this.posZ.get(srcIndex));

        dst.rotX.put(dstIndex, this.rotX.get(srcIndex));
        dst.rotY.put(dstIndex, this.rotY.get(srcIndex));
        dst.rotZ.put(dstIndex, this.rotZ.get(srcIndex));
        dst.rotW.put(dstIndex, this.rotW.get(srcIndex));

        dst.velX.put(dstIndex, this.velX.get(srcIndex));
        dst.velY.put(dstIndex, this.velY.get(srcIndex));
        dst.velZ.put(dstIndex, this.velZ.get(srcIndex));

        dst.angVelX.put(dstIndex, this.angVelX.get(srcIndex));
        dst.angVelY.put(dstIndex, this.angVelY.get(srcIndex));
        dst.angVelZ.put(dstIndex, this.angVelZ.get(srcIndex));

        dst.aabbMinX.put(dstIndex, this.aabbMinX.get(srcIndex));
        dst.aabbMinY.put(dstIndex, this.aabbMinY.get(srcIndex));
        dst.aabbMinZ.put(dstIndex, this.aabbMinZ.get(srcIndex));
        dst.aabbMaxX.put(dstIndex, this.aabbMaxX.get(srcIndex));
        dst.aabbMaxY.put(dstIndex, this.aabbMaxY.get(srcIndex));
        dst.aabbMaxZ.put(dstIndex, this.aabbMaxZ.get(srcIndex));

        dst.isActive.put(dstIndex, this.isActive.get(srcIndex));
        dst.stateTick.put(dstIndex, this.stateTick.get(srcIndex));
    }
}
