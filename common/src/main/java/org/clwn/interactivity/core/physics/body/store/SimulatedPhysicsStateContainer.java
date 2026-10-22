package org.clwn.interactivity.core.physics.body.store;

import lombok.Getter;
import lombok.Setter;
import org.clwn.interactivity.core.util.buffer.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

/**
 * State container of a world that runs the Jolt simulation.
 * <p>
 * Besides the base physics state it holds everything that only makes sense next to a native body:
 * the Jolt body id, the chunk the body is currently tracked in, and the dirty tracking used by network synchronization.
 */
public class SimulatedPhysicsStateContainer extends PhysicsStateContainer {
    /**
     * Value of {@link #joltBodyId} for slots that have no Jolt body (equals {@code JPH::BodyID::cInvalidBodyID}).
     */
    public static final int INVALID_JOLT_BODY_ID = -1;

    /**
     * Whether the slot is already listed in {@link #dirtyIndices} for the current tick.
     * <p>
     * Doubles as the membership test, so a slot is never listed twice no matter who marks it
     * (the native sync, or Java code through {@code BatchPhysicsSync#markDirty}).
     */
    public final ByteBuffer isDirty;

    /**
     * Slots that changed since the last network update, in no particular order.
     * Only the first {@link #dirtyCount} entries are valid.
     */
    public final IntBuffer dirtyIndices;

    /**
     * Number of valid entries in {@link #dirtyIndices}.
     */
    @Getter @Setter
    private int dirtyCount;

    /**
     * Jolt body id of each slot, {@link #INVALID_JOLT_BODY_ID} when the slot has no Jolt body yet.
     */
    public final IntBuffer joltBodyId;

    /**
     * Packed chunk position ({@link net.minecraft.world.level.ChunkPos#asLong}) each body is currently tracked in.
     */
    public final LongBuffer chunkKey;

    public SimulatedPhysicsStateContainer(int capacity) {
        super(capacity);
        this.isDirty = ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
        this.dirtyIndices = ByteBuffer.allocateDirect(capacity * Integer.BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();
        this.joltBodyId = ByteBuffer.allocateDirect(capacity * Integer.BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();
        this.chunkKey = ByteBuffer.allocateDirect(capacity * Long.BYTES).order(ByteOrder.nativeOrder()).asLongBuffer();

        for (int i = 0; i < capacity; i++) {
            this.joltBodyId.put(i, INVALID_JOLT_BODY_ID);
            this.chunkKey.put(i, Long.MAX_VALUE);
        }
    }

    public boolean isDirty(int index) {
        return this.isDirty.get(index) != 0;
    }

    @Override
    public void copyTo(PhysicsStateContainer other) {
        super.copyTo(other);

        if (other instanceof SimulatedPhysicsStateContainer simulated) {
            int copyLength = Math.min(this.capacity, other.capacity);

            BufferUtils.copyByteBuffer(this.isDirty, simulated.isDirty, copyLength);
            BufferUtils.copyIntBuffer(this.dirtyIndices, simulated.dirtyIndices, copyLength);
            BufferUtils.copyIntBuffer(this.joltBodyId, simulated.joltBodyId, copyLength);
            BufferUtils.copyLongBuffer(this.chunkKey, simulated.chunkKey, copyLength);
            simulated.dirtyCount = this.dirtyCount;
        }
    }

    @Override
    public void reset(int index) {
        super.reset(index);

        this.isDirty.put(index, (byte) 0);
        this.joltBodyId.put(index, INVALID_JOLT_BODY_ID);
        this.chunkKey.put(index, Long.MAX_VALUE);
    }
}
