package org.clwn.interactivity.api.physics.body;

import org.clwn.interactivity.api.physics.world.PhysicsWorld;

import java.util.UUID;

public abstract class Body {
    private final PhysicsWorld world;
    private final UUID uuid;
    private final BodyType<? extends Body> type;

    protected Body(PhysicsWorld world, UUID uuid, BodyType<? extends Body> type) {
        this.world = world;
        this.uuid = uuid;
        this.type = type;
    }

    public final PhysicsWorld getWorld() {
        return world;
    }

    public final UUID getUuid() {
        return uuid;
    }

    public final BodyType<? extends Body> getType() {
        return type;
    }

    /** Called after the body has been constructed and inserted into runtime state. */
    protected void initialize() {
    }
}
