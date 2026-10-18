/*
 * Copyright (c) 2025-2026 xI-Mx-Ix
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.body;

import com.github.stephengold.joltjni.Vec3;
import com.github.stephengold.joltjni.enumerate.EActivation;
import com.github.stephengold.joltjni.enumerate.EMotionType;
import com.github.stephengold.joltjni.readonly.QuatArg;
import com.github.stephengold.joltjni.readonly.RVec3Arg;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyManager;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.clwn.interactivity.core.physics.body.store.AbstractBodyDataStore;
import org.clwn.interactivity.core.physics.body.store.PhysicsStateContainer;
import org.clwn.interactivity.core.physics.body.type.BodyTypeImpl;
import org.clwn.interactivity.core.physics.world.PhysicsWorldImpl;

import java.util.UUID;
import java.util.function.Consumer;


public abstract class AbstractBodyManager implements BodyManager {
    public abstract AbstractBodyDataStore getBodyDataStore();
    // todo: save manager

    protected PhysicsWorldImpl physicsWorld;

    public AbstractBodyManager(PhysicsWorldImpl physicsWorld) {
        this.physicsWorld = physicsWorld;
    }

    @Override
    public <T extends Body> T spawnBody(BodyType<T> bodyType, RVec3Arg position, QuatArg rotation, Consumer<T> configurator) {
        return spawnBody(bodyType, UUID.randomUUID(), position, rotation, configurator);
    }

    public <T extends Body> T spawnBody(BodyType<T> bodyType, UUID uuid, RVec3Arg position, QuatArg rotation, Consumer<T> configurator) {
        T body = bodyType.create(physicsWorld, uuid);

        configurator.accept(body);
        spawnConstructedBody(body, position, rotation, new Vec3(0, 0, 0), new Vec3(0, 0, 0));
        return body;
    }

    public <T extends Body> void spawnConstructedBody(T body, RVec3Arg position, QuatArg rotation, Vec3 linearVelocity, Vec3 angularVelocity) {
        int index = spawnInternal(body);
        PhysicsStateContainer container = getBodyDataStore().getPhysicsStateContainer();

        container.posX.put(index, position.x());
        container.posY.put(index, position.y());
        container.posZ.put(index, position.z());
        container.rotX.put(index, rotation.getX());
        container.rotY.put(index, rotation.getY());
        container.rotZ.put(index, rotation.getZ());
        container.rotW.put(index, rotation.getW());

        // todo: using spatial manager like in Velthoric will be very useful for netsync and (un-)loading, but there might be better ways to do it, so approach should be selected
//        long oldKey = c.chunkKey.get(index);
//        long newKey = VxSpatialManager.calculateChunkKey(c.posX.get(index), c.posZ.get(index));
//        if (oldKey != newKey) {
//            c.chunkKey.put(index, newKey);
//            spatialManager.move(body, oldKey, newKey);
//        }

        finalizeBodyAddition(body, position, rotation, linearVelocity, angularVelocity, EActivation.Activate, EMotionType.Dynamic);
    }

    private <T extends Body> int spawnInternal(T body) {
        // todo: support both rigid and soft bodies. While there is RigidBodyBehavior which controls creation, here we are assuming that all bodies are rigid

        int index = getBodyDataStore().addBody(body);

        PhysicsStateContainer container = getBodyDataStore().getPhysicsStateContainer();

        container.isActive.put(index, (byte) 1);

        // --- Behavior Attachment ---
        // Apply all default behaviors from the body type

        container.behaviorFlags.put(index, ((BodyTypeImpl<?>) body.getType()).getBehaviorMask());

        // Initialize spatial tracking
//        long chunkKey = VxSpatialManager.calculateChunkKey(container.posX.get(index), container.posZ.get(index));
//        container.chunkKey.put(index, chunkKey);
//        spatialManager.add(chunkKey, body);

        return index;
    }

    private <T extends Body> void finalizeBodyAddition(T body, RVec3Arg position, QuatArg rotation, Vec3 linearVelocity, Vec3 angularVelocity, EActivation activation, EMotionType motionType) {
        // todo: notify network manager and netsync

        // Dispatch Jolt creation based on body type's provider
        // todo: rigid + soft; remove if bodyId == -1 (fail); send events
        this.physicsWorld.getJoltBridge().createAndAddJoltRigidBody(body, position, rotation, linearVelocity, angularVelocity, activation, motionType);
    }

    @Override
    public void removeBody(Body body) {

    }

    @Override
    public Body getBody(UUID id) {
        return getBodyDataStore().getPhysicsStateContainer().bodies[getBodyDataStore().getIndexByUuid(id)];
    }
}
