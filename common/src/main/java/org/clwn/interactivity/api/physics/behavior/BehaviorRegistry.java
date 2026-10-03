package org.clwn.interactivity.api.physics.behavior;

import java.util.Set;

public interface BehaviorRegistry {
    <B extends Behavior> void registerBehavior(Class<B> behaviorClass);

    boolean isRegistered(Class<? extends Behavior> behaviorClass);

    Set<Class<? extends Behavior>> getRegisteredBehaviors();
}
