/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.event.contact;

import com.github.stephengold.joltjni.readonly.RVec3Arg;
import com.github.stephengold.joltjni.readonly.Vec3Arg;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;
import org.jetbrains.annotations.Nullable;

public class ContactAddedEvent extends ContactPointEvent {
    public ContactAddedEvent(PhysicsWorld physicsWorld, Body body, @Nullable Body other, boolean terrain,
                            RVec3Arg point, Vec3Arg normal, float penetration, float approachSpeed, int otherSubShapeUserData) {
        super(physicsWorld, body, other, terrain, point, normal, penetration, approachSpeed, otherSubShapeUserData);
    }
}
