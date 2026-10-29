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

    public static final class PrePhysicsTickEvent extends PhysicsTickEvent {
        public PrePhysicsTickEvent(PhysicsWorld physicsWorld, float deltaTime) {
            super(physicsWorld, deltaTime);
        }
    }

    public static final class PostPhysicsTickEvent extends PhysicsTickEvent {
        public PostPhysicsTickEvent(PhysicsWorld physicsWorld, float deltaTime) {
            super(physicsWorld, deltaTime);
        }
    }
}
