package org.clwn.interactivity.core.physics.body.store;

public abstract class SimulatedBodyDataStore extends AbstractBodyDataStore {
    @Override
    public abstract SimulatedPhysicsStateContainer getPhysicsStateContainer();
}
