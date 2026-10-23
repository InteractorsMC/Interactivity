/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.body.type;

import net.minecraft.resources.ResourceLocation;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.clwn.interactivity.api.physics.behavior.Behavior;
import org.clwn.interactivity.core.physics.behavior.BehaviorRegistryImpl;

import java.util.LinkedHashMap;
import java.util.Map;

final class BodyTypeBuilderImpl<T extends Body> implements BodyType.Builder<T> {
    private final Map<Class<? extends Behavior>, Behavior> behaviors = new LinkedHashMap<>();

    @Override
    public BodyType.Builder<T> addBehavior(Behavior behavior) {
        if (behavior == null) {
            throw new IllegalArgumentException("behavior cannot be null");
        }

        Class<? extends Behavior> behaviorClass = behavior.getClass();
        Behavior previous = behaviors.putIfAbsent(behaviorClass, behavior);
        if (previous != null) {
            throw new IllegalArgumentException(
                    "Behavior is already configured for this BodyType: " + behaviorClass.getName()
            );
        }

        return this;
    }

    @Override
    public BodyType.Builder<T> removeBehavior(Class<? extends Behavior> behaviorClass) {
        behaviors.remove(behaviorClass);
        return this;
    }

    BodyTypeImpl<T> build(ResourceLocation typeId, Class<T> bodyClass, BodyType.Factory<T> factory, BehaviorRegistryImpl behaviorRegistry) {
        if (typeId == null) {
            throw new IllegalArgumentException("typeId cannot be null");
        }
        if (factory == null) {
            throw new IllegalArgumentException("factory cannot be null");
        }

        long behaviorMask = 0L;
        for (Class<? extends Behavior> behaviorClass : behaviors.keySet()) {
            if (!behaviorRegistry.isRegistered(behaviorClass)) {
                throw new IllegalStateException("Behavior is not registered: " + behaviorClass.getName());
            }
            behaviorMask |= behaviorRegistry.getMask(behaviorClass);
        }

        return new BodyTypeImpl<>(typeId, bodyClass, factory, behaviors, behaviorMask);
    }
}
