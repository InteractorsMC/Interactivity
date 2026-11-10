package org.clwn.interactivity.api.physics.event;

import lombok.Getter;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;

public abstract class PhysicsTickEvent extends PhysicsWorldEvent {
    @Getter
    protected final float deltaTime;

    public PhysicsTickEvent(PhysicsWorld physicsWorld, float deltaTime) {
        super(physicsWorld);
        this.deltaTime = deltaTime;
    }

    public static final class PreTick extends PhysicsTickEvent {
        public PreTick(PhysicsWorld physicsWorld, float deltaTime) {
            super(physicsWorld, deltaTime);
        }
    }

    public static final class PostTick extends PhysicsTickEvent {
        public PostTick(PhysicsWorld physicsWorld, float deltaTime) {
            super(physicsWorld, deltaTime);
        }
    }
}
