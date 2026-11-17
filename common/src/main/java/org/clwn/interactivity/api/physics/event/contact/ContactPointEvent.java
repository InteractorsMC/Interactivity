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
import lombok.Getter;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;
import org.jetbrains.annotations.Nullable;

/**
 * A contact with its contact point, as detected before the contact was solved.
 */
public abstract class ContactPointEvent extends ContactEvent {
    /**
     * World space contact point on the body.
     */
    @Getter
    protected final RVec3Arg point;

    /**
     * Points from the body to the other body.
     */
    @Getter
    protected final Vec3Arg normal;

    /**
     * Negative for speculative contacts that don't touch yet.
     */
    @Getter
    protected final float penetration;

    /**
     * Speed the bodies move towards each other along the normal, negative when they separate.
     */
    @Getter
    protected final float approachSpeed;

    /**
     * User data of the touched sub shape of the other body, for terrain it is the block tag.
     */
    @Getter
    protected final int otherSubShapeUserData;

    protected ContactPointEvent(PhysicsWorld physicsWorld, Body body, @Nullable Body other, boolean terrain,
                                RVec3Arg point, Vec3Arg normal, float penetration, float approachSpeed, int otherSubShapeUserData) {
        super(physicsWorld, body, other, terrain);
        this.point = point;
        this.normal = normal;
        this.penetration = penetration;
        this.approachSpeed = approachSpeed;
        this.otherSubShapeUserData = otherSubShapeUserData;
    }
}
