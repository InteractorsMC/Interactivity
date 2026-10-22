package org.clwn.interactivity.core.physics.body.store;

import lombok.Getter;

public class ServerBodyDataStore extends SimulatedBodyDataStore {
    private static final int HISTORY_SIZE = 0;

    @Getter
    SimulatedPhysicsStateContainer physicsStateContainer;

    @Getter
    HistoricalPhysicsStateContainer history;

    public ServerBodyDataStore() {
        super();
    }

    @Override
    protected void allocate(int newCapacity) {
        SimulatedPhysicsStateContainer newContainer = new SimulatedPhysicsStateContainer(newCapacity);

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
}
