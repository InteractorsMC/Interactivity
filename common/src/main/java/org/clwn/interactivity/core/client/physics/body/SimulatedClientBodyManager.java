package org.clwn.interactivity.core.client.physics.body;

import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.clwn.interactivity.core.physics.body.SimulatedBodyManager;
import org.clwn.interactivity.core.physics.body.store.SimulatedBodyDataStore;
import org.clwn.interactivity.core.physics.network.BodyStateCodec;
import org.clwn.interactivity.core.physics.world.SimulatedPhysicsWorldImpl;

import java.util.UUID;

public class SimulatedClientBodyManager extends SimulatedBodyManager implements ClientBodyManager {
    public SimulatedClientBodyManager(SimulatedPhysicsWorldImpl physicsWorld) {
        super(physicsWorld);
        throw new UnsupportedOperationException("The simulated client world is not implemented yet");
    }

    @Override
    public SimulatedBodyDataStore getBodyDataStore() {
        return null;
    }

    @Override
    public void addRemote(int networkId, UUID uuid, BodyType<? extends Body> type, BodyStateCodec.Record state, int tick) {

    }

    @Override
    public void removeRemote(int networkId) {

    }

    @Override
    public void applyState(BodyStateCodec.Record state, int tick) {

    }
}
