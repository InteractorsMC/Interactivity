/*
 * Copyright (c) 2025 xI-Mx-Ix
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.body;

import net.minecraft.resources.ResourceLocation;
import org.clwn.interactivity.api.physics.behavior.Behavior;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface BodyType<T extends Body> {
    ResourceLocation getTypeId();

    T create(PhysicsWorld world, UUID uuid);

    Set<Behavior> getBehaviors();

    default boolean hasBehavior(Class<? extends Behavior> behaviorClass) {
        return getBehavior(behaviorClass).isPresent();
    }

    <B extends Behavior> Optional<B> getBehavior(Class<B> behaviorClass);

    interface Factory<T extends Body> {
        T create(BodyType<T> type, PhysicsWorld world, UUID uuid);
    }

    interface Builder<T extends Body> {
        Builder<T> addBehavior(Behavior behavior);

        Builder<T> removeBehavior(Class<? extends Behavior> behaviorClass);

        default Builder<T> addBehaviors(Behavior... behaviors) {
            for (Behavior behavior : behaviors) {
                addBehavior(behavior);
            }
            return this;
        }
    }

    static <T extends Body> Set<Behavior> immutableBehaviors(Set<Behavior> behaviors) {
        return Collections.unmodifiableSet(behaviors);
    }
}
