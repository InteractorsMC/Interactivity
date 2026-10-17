package org.clwn.interactivity.core.physics.body.store;

import lombok.Getter;

public class ServerBodyDataStore extends AbstractBodyDataStore {
    @Getter
    SimulatedPhysicsStateContainer physicsStateContainer;

    public ServerBodyDataStore() {
        super();
    }

    @Override
    protected void allocate(int newCapacity) {
        SimulatedPhysicsStateContainer newContainer = new SimulatedPhysicsStateContainer(newCapacity);

        if (physicsStateContainer != null) {
            physicsStateContainer.copyTo(newContainer);
        }

        this.physicsStateContainer = newContainer;
        this.capacity = newCapacity;
    }
}
