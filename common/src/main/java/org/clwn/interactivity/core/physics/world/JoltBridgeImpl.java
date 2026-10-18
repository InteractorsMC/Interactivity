/*
 * Copyright (c) 2025-2026 xI-Mx-Ix
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.world;

import com.github.stephengold.joltjni.Jolt;
import com.github.stephengold.joltjni.PhysicsSystem;
import com.github.stephengold.joltjni.Vec3;
import com.github.stephengold.joltjni.enumerate.EActivation;
import com.github.stephengold.joltjni.enumerate.EMotionType;
import com.github.stephengold.joltjni.readonly.QuatArg;
import com.github.stephengold.joltjni.readonly.RVec3Arg;
import lombok.Getter;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.api.physics.behavior.RigidBehavior;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.RigidBodyFactory;
import org.clwn.interactivity.api.physics.body.RigidProvider;
import org.clwn.interactivity.api.physics.world.JoltBridge;

import java.util.Optional;

public class JoltBridgeImpl implements JoltBridge {
    @Getter
    private final PhysicsSystem physicsSystem;

    public JoltBridgeImpl(PhysicsSystem physicsSystem) {
        this.physicsSystem = physicsSystem;
    }

    public <T extends Body> int createAndAddJoltRigidBody(T body, RVec3Arg position, QuatArg rotation, Vec3 linearVelocity, Vec3 angularVelocity, EActivation activation, EMotionType motionType) {
        try {
            Optional<RigidBehavior> behavior = body.getType().getBehavior(RigidBehavior.class);

            if (behavior.isEmpty()) {
                Interactivity.LOGGER.error("Body type {} has no rigid behavior", body.getType().getTypeId());
                return -1;
            }

            RigidProvider provider = behavior.get().rigidProvider();

            RigidBodyFactory factory = (shape, bcs) -> {
                bcs.setShape(shape.create().get());
                bcs.setPosition(position);
                bcs.setRotation(rotation);

                bcs.setLinearVelocity(linearVelocity);
                bcs.setAngularVelocity(angularVelocity);

                // Set the exact motion type requested by the body configuration
                bcs.setMotionType(motionType);

                // Ensure MotionProperties are created even for static bodies to allow future state transitions
                bcs.setAllowDynamicOrKinematic(true);

                return this.physicsSystem.getBodyInterface().createAndAddBody(bcs, activation);
            };

            int bodyId = provider.createJoltBody(body, factory);

            if (bodyId == Jolt.cInvalidBodyId) {
                Interactivity.LOGGER.error("Jolt failed to create/add rigid body for {}", body.getPhysicsId());
                return -1;
            }

            body.setPhysicsId(bodyId);

            return bodyId;

        } catch (Exception e) {
            Interactivity.LOGGER.error("Failed to create/add rigid body {}", body.getPhysicsId(), e);
            return -1;
        }
    }
}
