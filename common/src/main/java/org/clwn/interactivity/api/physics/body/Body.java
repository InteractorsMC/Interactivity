/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.body;

import lombok.Getter;
import lombok.Setter;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;
import org.clwn.interactivity.api.physics.world.ServerPhysicsWorld;
import org.clwn.interactivity.api.util.math.Quat;
import org.clwn.interactivity.api.util.math.Vec3d;
import org.clwn.interactivity.api.util.math.Vec3f;

import java.util.UUID;

public abstract class Body {
    @Getter
    private final PhysicsWorld physicsWorld;

    @Getter
    private final UUID uuid;

    @Getter
    private final BodyType<? extends Body> type;

    @Getter @Setter
    private int physicsId;

    protected Body(BodyType<? extends Body> type, PhysicsWorld physicsWorld, UUID uuid) {
        this.physicsWorld = physicsWorld;
        this.uuid = uuid;
        this.type = type;
    }

    public Vec3d getPosition() {
        return this.physicsWorld.getPhysicsStateAccessor().getPosition(this.uuid);
    }

    public Quat getRotation() {
        return this.physicsWorld.getPhysicsStateAccessor().getRotation(this.uuid);
    }

    public Vec3f getLinearVelocity() {
        return this.physicsWorld.getPhysicsStateAccessor().getLinearVelocity(this.uuid);
    }

    public Vec3f getAngularVelocity() {
        return this.physicsWorld.getPhysicsStateAccessor().getAngularVelocity(this.uuid);
    }

    public void requestDataSync() {
        if (this.physicsWorld instanceof ServerPhysicsWorld serverWorld) {
            serverWorld.getBodyManager().requestDataSync(this);
        }
    }
}
