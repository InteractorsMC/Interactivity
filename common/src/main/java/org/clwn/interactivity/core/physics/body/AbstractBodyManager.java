package org.clwn.interactivity.core.physics.body;

import org.clwn.interactivity.api.physics.body.AddReason;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyManager;
import org.clwn.interactivity.api.physics.body.RemovalReason;
import org.clwn.interactivity.core.physics.body.store.AbstractBodyDataStore;
import org.clwn.interactivity.core.physics.system.BodyEventDispatcher;
import org.clwn.interactivity.core.physics.world.PhysicsWorldImpl;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


public abstract class AbstractBodyManager implements BodyManager {
    protected final PhysicsWorldImpl physicsWorld;

    public AbstractBodyManager(PhysicsWorldImpl physicsWorld) {
        this.physicsWorld = physicsWorld;
    }

    public abstract AbstractBodyDataStore getBodyDataStore();

    @Override
    public void requestDataSync(Body body) {
        // Only the server sends body data
    }

    @Override
    public @Nullable Body getBody(UUID id) {
        AbstractBodyDataStore store = getBodyDataStore();
        int index = store.getIndexByUuid(id);
        return index == -1 ? null : store.getPhysicsStateContainer().bodies[index];
    }

    protected void onBodyAdd(Body body, int index, AddReason addReason) {
        BodyEventDispatcher.onBodyAdded(body, addReason);
    }

    protected void onBodyRemove(Body body, int index, RemovalReason removalReason) {
        BodyEventDispatcher.onBodyRemoved(body, removalReason);
    }

    // todo on body activate/deactivate
}
