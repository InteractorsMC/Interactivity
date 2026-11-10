package org.clwn.interactivity.api.physics.event;

import lombok.Getter;
import org.clwn.interactivity.api.physics.body.AddReason;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.RemovalReason;

public abstract class BodyLifecycleEvent extends BodyEvent {
    public BodyLifecycleEvent(Body body) {
        super(body.getPhysicsWorld(), body);
    }

    public static final class Added extends BodyLifecycleEvent {
        @Getter
        private final AddReason addReason;

        public Added(Body body, AddReason addReason) {
            super(body);
            this.addReason = addReason;
        }
    }

    public static final class Removed extends BodyLifecycleEvent {
        @Getter
        private final RemovalReason removalReason;

        public Removed(Body body, RemovalReason removalReason) {
            super(body);
            this.removalReason = removalReason;
        }
    }

    public static final class Activated extends BodyLifecycleEvent {
        public Activated(Body body) {
            super(body);
        }
    }

    public static final class Deactivated extends BodyLifecycleEvent {
        public Deactivated(Body body) {
            super(body);
        }
    }
}
