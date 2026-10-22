package org.clwn.interactivity.core.physics.body;

import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyManager;
import org.clwn.interactivity.core.physics.body.store.AbstractBodyDataStore;
import org.clwn.interactivity.core.physics.world.PhysicsWorldImpl;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


public abstract class AbstractBodyManager implements BodyManager {
    // todo: save manager

    protected final PhysicsWorldImpl physicsWorld;

    public AbstractBodyManager(PhysicsWorldImpl physicsWorld) {
        this.physicsWorld = physicsWorld;
    }

    public abstract AbstractBodyDataStore getBodyDataStore();

    @Override
    public @Nullable Body getBody(UUID id) {
        AbstractBodyDataStore store = getBodyDataStore();
        int index = store.getIndexByUuid(id);
        return index == -1 ? null : store.getPhysicsStateContainer().bodies[index];
    }
}
