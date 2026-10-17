/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.body;

import org.clwn.interactivity.api.util.math.Quat;
import org.clwn.interactivity.api.util.math.Vec3d;
import org.clwn.interactivity.api.util.math.Vec3f;

import java.util.UUID;

/**
 * Allows to access the physics states of bodies.
 */
public interface PhysicsStateAccessor {
    default Vec3d getPosition(Body body) {
        return getPosition(body.getUuid());
    }
    Vec3d getPosition(UUID bodyId);

    default Quat getRotation(Body body) {
        return getRotation(body.getUuid());
    }
    Quat getRotation(UUID bodyId);

    default Vec3f getLinearVelocity(Body body) {
        return getLinearVelocity(body.getUuid());
    }
    Vec3f getLinearVelocity(UUID bodyId);

    default Vec3f getAngularVelocity(Body body) {
        return getAngularVelocity(body.getUuid());
    }
    Vec3f getAngularVelocity(UUID bodyId);

    default boolean isActive(Body body) {
        return isActive(body.getUuid());
    }
    boolean isActive(UUID bodyId);

    default long getBehaviorFlags(Body body) {
        return getBehaviorFlags(body.getUuid());
    }
    long getBehaviorFlags(UUID bodyId);
}
