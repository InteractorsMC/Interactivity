package org.clwn.interactivity.api.physics.event.body;

import lombok.Getter;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.event.PhysicsWorldEvent;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;

public class BodyEvent extends PhysicsWorldEvent {
    @Getter
    protected final Body body;

    public BodyEvent(PhysicsWorld physicsWorld, Body body) {
        super(physicsWorld);
        this.body = body;
    }
}
