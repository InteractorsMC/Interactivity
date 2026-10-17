package org.clwn.interactivity.core.physics.body.store;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SimulatedPhysicsStateContainer extends PhysicsStateContainer implements ISimulatedPhysicsStateContainer {
    /**
     * A set of all indices currently marked as dirty for the next network tick.
     * <p>
     * This set is used to track which bodies have changed and need to be synchronized with clients.
     * </p>
     */
    public final ByteBuffer isDirty;

    public SimulatedPhysicsStateContainer(int capacity) {
        super(capacity);
        this.isDirty = ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
    }

    @Override
    public boolean isDirty(int index) {
        return this.isDirty.get(index) != 0;
    }
}
