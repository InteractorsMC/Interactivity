/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.body;

import com.github.stephengold.joltjni.*;
import com.github.stephengold.joltjni.enumerate.EActivation;
import com.github.stephengold.joltjni.enumerate.EMotionType;
import com.github.stephengold.joltjni.readonly.QuatArg;
import com.github.stephengold.joltjni.readonly.RVec3Arg;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.api.physics.body.AddReason;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.clwn.interactivity.api.physics.body.RemovalReason;
import org.clwn.interactivity.core.physics.body.store.SimulatedBodyDataStore;
import org.clwn.interactivity.core.physics.body.store.SimulatedPhysicsStateContainer;
import org.clwn.interactivity.core.physics.body.type.BodyTypeImpl;
import org.clwn.interactivity.core.physics.system.BodyEventDispatcher;
import org.clwn.interactivity.core.physics.world.SimulatedPhysicsWorldImpl;

import java.util.UUID;
import java.util.function.Consumer;


public abstract class SimulatedBodyManager extends AbstractBodyManager {
    protected final SimulatedPhysicsWorldImpl simulatedWorld;

    public SimulatedBodyManager(SimulatedPhysicsWorldImpl physicsWorld) {
        super(physicsWorld);
        this.simulatedWorld = physicsWorld;
    }

    @Override
    public abstract SimulatedBodyDataStore getBodyDataStore();

    @Override
    public <T extends Body> T spawnBody(BodyType<T> bodyType, RVec3Arg position, QuatArg rotation, Consumer<T> configurator) {
        return spawnBody(bodyType, UUID.randomUUID(), position, rotation, configurator);
    }

    public <T extends Body> T spawnBody(BodyType<T> bodyType, UUID uuid, RVec3Arg position, QuatArg rotation, Consumer<T> configurator) {
        T body = bodyType.create(simulatedWorld, uuid);

        configurator.accept(body);
        spawnConstructedBody(body, position, rotation, new Vec3(0, 0, 0), new Vec3(0, 0, 0));
        return body;
    }

    /**
     * Queues a fully constructed body for addition to the world.
     * The arguments are copied, so the caller may reuse them right after the call.
     */
    public <T extends Body> void spawnConstructedBody(T body, RVec3Arg position, QuatArg rotation, Vec3 linearVelocity, Vec3 angularVelocity) {
        double px = position.xx(), py = position.yy(), pz = position.zz();
        float qx = rotation.getX(), qy = rotation.getY(), qz = rotation.getZ(), qw = rotation.getW();
        float lvx = linearVelocity.getX(), lvy = linearVelocity.getY(), lvz = linearVelocity.getZ();
        float avx = angularVelocity.getX(), avy = angularVelocity.getY(), avz = angularVelocity.getZ();

        simulatedWorld.execute(() -> spawnBodyInternal(
                body,
                px, py, pz,
                qx, qy, qz, qw,
                lvx, lvy, lvz,
                avx, avy, avz,
                EActivation.Activate,
                AddReason.SPAWNED
        ));
    }

    @Override
    public void removeBody(UUID id, RemovalReason removalReason) {
        simulatedWorld.execute(() -> removeBodyInternal(id, removalReason));
    }

    public boolean spawnBodyInternal(
            Body body,
            double px, double py, double pz,
            float qx, float qy, float qz, float qw,
            float lvx, float lvy, float lvz,
            float avx, float avy, float avz,
            EActivation activation,
            AddReason addReason
    ) {
        // todo: support both rigid and soft bodies. While there is RigidBodyBehavior which controls creation, here we are assuming that all bodies are rigid
        SimulatedBodyDataStore store = getBodyDataStore();

        int index = store.addBody(body);
        SimulatedPhysicsStateContainer container = store.getPhysicsStateContainer();

        container.posX.put(index, px);
        container.posY.put(index, py);
        container.posZ.put(index, pz);
        container.rotX.put(index, qx);
        container.rotY.put(index, qy);
        container.rotZ.put(index, qz);
        container.rotW.put(index, qw);
        container.velX.put(index, lvx);
        container.velY.put(index, lvy);
        container.velZ.put(index, lvz);
        container.angVelX.put(index, avx);
        container.angVelY.put(index, avy);
        container.angVelZ.put(index, avz);
        container.isActive.put(index, (byte) (activation == EActivation.Activate ? 1 : 0));

        // Apply all default behaviors from the body type
        container.behaviorFlags.put(index, ((BodyTypeImpl<?>) body.getType()).getBehaviorMask());

        // todo: rigid + soft; send events
        int bodyId = simulatedWorld.getJoltBridge().createAndAddJoltRigidBody(
                body,
                new RVec3(px, py, pz),
                new Quat(qx, qy, qz, qw),
                new Vec3(lvx, lvy, lvz),
                new Vec3(avx, avy, avz),
                activation,
                EMotionType.Dynamic
        );

        if (bodyId == -1) {
            Interactivity.LOGGER.error("Failed to add body {} of type {}", body.getUuid(), body.getType().getTypeId());
            store.removeBody(body.getUuid());
            return false;
        }

        container.joltBodyId.put(index, bodyId);

        try (
                TransformedShape shape = simulatedWorld.getJoltBridge().getPhysicsSystem().getBodyInterface().getTransformedShape(bodyId);
                AaBox bounds = shape.getWorldSpaceBounds()
        ) {
            Vec3 min = bounds.getMin();
            Vec3 max = bounds.getMax();
            container.aabbMinX.put(index, min.getX());
            container.aabbMinY.put(index, min.getY());
            container.aabbMinZ.put(index, min.getZ());
            container.aabbMaxX.put(index, max.getX());
            container.aabbMaxY.put(index, max.getY());
            container.aabbMaxZ.put(index, max.getZ());
        }

        onBodyAdd(body, index, addReason);
        return true;
    }

    public void removeBodyInternal(UUID uuid, RemovalReason removalReason) {
        SimulatedBodyDataStore store = getBodyDataStore();

        int index = store.getIndexByUuid(uuid);
        if (index == -1) {
            return;
        }

        SimulatedPhysicsStateContainer container = store.getPhysicsStateContainer();
        Body body = container.bodies[index];

        onBodyRemove(body, index, removalReason);
        simulatedWorld.getConstraintManager().onBodyRemoving(uuid);

        int bodyId = container.joltBodyId.get(index);
        if (bodyId != Jolt.cInvalidBodyId) {
            simulatedWorld.getJoltBridge().destroyJoltBody(bodyId);
        }

        store.removeBody(uuid);
    }

    public int getJoltBodyId(UUID id) {
        int index = getBodyDataStore().getIndexByUuid(id);
        return index == -1 ? Jolt.cInvalidBodyId : getBodyDataStore().getPhysicsStateContainer().joltBodyId.get(index);

    }

    protected void onBodyAdd(Body body, int index, AddReason addReason) {
        BodyEventDispatcher.onBodyAdded(body, addReason);
    }

    protected void onBodyRemove(Body body, int index, RemovalReason removalReason) {
        BodyEventDispatcher.onBodyRemoved(body, removalReason);
    }
}
