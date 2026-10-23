/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.body.type;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.clwn.interactivity.api.physics.behavior.Behavior;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class BodyTypeImpl<T extends Body> implements BodyType<T> {
    private final ResourceLocation typeId;
    private final BodyType.Factory<T> factory;
    private final Map<Class<? extends Behavior>, Behavior> behaviors;
    @Getter
    private final long behaviorMask;

    @Getter
    private final Class<T> bodyClass;

    BodyTypeImpl(
            ResourceLocation typeId,
            Class<T> bodyClass,
            BodyType.Factory<T> factory,
            Map<Class<? extends Behavior>, Behavior> behaviors,
            long behaviorMask
    ) {
        this.typeId = typeId;
        this.bodyClass = bodyClass;
        this.factory = factory;
        this.behaviors = Map.copyOf(new LinkedHashMap<>(behaviors));
        this.behaviorMask = behaviorMask;
    }

    @Override
    public ResourceLocation getTypeId() {
        return typeId;
    }

    @Override
    public T create(PhysicsWorld world, UUID uuid) {
        T body = factory.create(this, world, uuid);
        if (body == null) {
            throw new IllegalStateException("Body factory returned null for " + typeId);
        }
        return body;
    }

    @Override
    public Set<Behavior> getBehaviors() {
        return Set.copyOf(behaviors.values());
    }

    @Override
    public <B extends Behavior> Optional<B> getBehavior(Class<B> behaviorClass) {
        Behavior behavior = behaviors.get(behaviorClass);
        if (behavior == null) {
            return Optional.empty();
        }
        return Optional.of(behaviorClass.cast(behavior));
    }
}
