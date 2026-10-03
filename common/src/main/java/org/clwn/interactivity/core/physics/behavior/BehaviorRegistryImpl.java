/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.behavior;

import org.clwn.interactivity.api.physics.behavior.Behavior;
import org.clwn.interactivity.api.physics.behavior.BehaviorRegistry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class BehaviorRegistryImpl implements BehaviorRegistry {
    public static final BehaviorRegistryImpl INSTANCE = new BehaviorRegistryImpl();
    public static final int MAX_BEHAVIORS = Long.SIZE;

    private final Map<Class<? extends Behavior>, Integer> bits = new LinkedHashMap<>();

    private BehaviorRegistryImpl() {
    }

    @Override
    public synchronized <B extends Behavior> void registerBehavior(Class<B> behaviorClass) {
        if (behaviorClass == null) {
            throw new IllegalArgumentException("behaviorClass cannot be null");
        }

        if (bits.containsKey(behaviorClass)) {
            return;
        }

        if (bits.size() >= MAX_BEHAVIORS) {
            throw new IllegalStateException("Maximum of 64 behavior types has been reached");
        }

        bits.put(behaviorClass, bits.size());
    }

    @Override
    public synchronized boolean isRegistered(Class<? extends Behavior> behaviorClass) {
        return bits.containsKey(behaviorClass);
    }

    @Override
    public synchronized Set<Class<? extends Behavior>> getRegisteredBehaviors() {
        return Set.copyOf(bits.keySet());
    }

    public synchronized long getMask(Class<? extends Behavior> behaviorClass) {
        Integer bit = bits.get(behaviorClass);
        if (bit == null) {
            throw new IllegalStateException("Behavior is not registered: " + behaviorClass.getName());
        }
        return 1L << bit;
    }
}
