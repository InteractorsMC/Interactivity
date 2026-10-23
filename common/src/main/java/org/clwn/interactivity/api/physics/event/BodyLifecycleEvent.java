package org.clwn.interactivity.api.physics.event;

import lombok.Getter;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.RemovalReason;

public abstract class BodyLifecycleEvent extends BodyEvent {
    public BodyLifecycleEvent(Body body) {
        super(body.getPhysicsWorld(), body);
    }

    public static final class BodyAddedEvent extends BodyLifecycleEvent {
        public BodyAddedEvent(Body body) {
            super(body);
        }
    }

    public static final class BodyRemovedEvent extends BodyLifecycleEvent {
        @Getter
        private final RemovalReason removalReason;

        public BodyRemovedEvent(Body body, RemovalReason removalReason) {
            super(body);
            this.removalReason = removalReason;
        }
    }

    public static final class BodyActivatedEvent extends BodyLifecycleEvent {
        public BodyActivatedEvent(Body body) {
            super(body);
        }
    }

    public static final class BodyDeactivatedEvent extends BodyLifecycleEvent {
        public BodyDeactivatedEvent(Body body) {
            super(body);
        }
    }
}
