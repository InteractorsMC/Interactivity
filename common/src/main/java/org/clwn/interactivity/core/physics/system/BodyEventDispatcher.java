package org.clwn.interactivity.core.physics.system;

import org.clwn.interactivity.api.physics.body.AddReason;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.RemovalReason;
import org.clwn.interactivity.api.physics.event.BodyLifecycleEvent;
import org.clwn.interactivity.core.physics.event.EventBusImpl;

public final class BodyEventDispatcher {
    private BodyEventDispatcher() {}

    public static void onBodyAdded(Body body, AddReason addReason) {
        EventBusImpl.INSTANCE.publishEvent(new BodyLifecycleEvent.BodyAddedEvent(body, addReason));
    }

    public static void onBodyRemoved(Body body, RemovalReason removalReason) {
        EventBusImpl.INSTANCE.publishEvent(new BodyLifecycleEvent.BodyRemovedEvent(body, removalReason));
    }

    public static void onBodyActivated(Body body) {
        EventBusImpl.INSTANCE.publishEvent(new BodyLifecycleEvent.BodyActivatedEvent(body));
    }

    public static void onBodyDeactivated(Body body) {
        EventBusImpl.INSTANCE.publishEvent(new BodyLifecycleEvent.BodyDeactivatedEvent(body));
    }
}
