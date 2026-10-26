/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.client.physics.body;

import com.github.stephengold.joltjni.Jolt;
import com.github.stephengold.joltjni.readonly.QuatArg;
import com.github.stephengold.joltjni.readonly.RVec3Arg;
import lombok.Getter;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.clwn.interactivity.api.physics.body.RemovalReason;
import org.clwn.interactivity.core.client.physics.body.store.EstimatedClientBodyDataStore;
import org.clwn.interactivity.core.client.physics.world.EstimatedClientPhysicsWorldImpl;
import org.clwn.interactivity.core.physics.body.AbstractBodyManager;
import org.clwn.interactivity.core.physics.body.store.PhysicsStateContainer;
import org.clwn.interactivity.core.physics.body.type.BodyTypeImpl;
import org.clwn.interactivity.core.physics.network.BodyStateCodec;

import java.util.UUID;
import java.util.function.Consumer;

public class EstimatedClientBodyManager extends AbstractBodyManager implements ClientBodyManager {
    @Getter
    private final EstimatedClientBodyDataStore bodyDataStore = new EstimatedClientBodyDataStore();

    private final EstimatedClientPhysicsWorldImpl physicsWorld;

    public EstimatedClientBodyManager(EstimatedClientPhysicsWorldImpl physicsWorld) {
        super(physicsWorld);
        this.physicsWorld = physicsWorld;
    }

    @Override
    public void addRemote(int networkId, UUID uuid, BodyType<? extends Body> type, BodyStateCodec.Record state, int tick) {
        if (bodyDataStore.getIndexByNetworkId(networkId) != -1) {
            removeRemote(networkId);
        }

        Body body = type.create(physicsWorld, uuid);

        int index = bodyDataStore.addBody(body);
        PhysicsStateContainer container = bodyDataStore.getPhysicsStateContainer();

        container.networkId.put(index, networkId);
        container.behaviorFlags.put(index, ((BodyTypeImpl<?>) type).getBehaviorMask());
        state.applyTo(container, index);
        container.stateTick.put(index, tick);

        bodyDataStore.getHistory().write(index, container);

        bodyDataStore.getNetworkIdToIndex().put(networkId, index);
    }

    @Override
    public void removeRemote(int networkId) {
        int index = bodyDataStore.getNetworkIdToIndex().remove(networkId);
        if (index == -1) {
            return;
        }

        UUID uuid = bodyDataStore.getUuidByIndex(index);
        if (uuid != null) {
            bodyDataStore.removeBody(uuid);
        }
    }

    @Override
    public void applyState(BodyStateCodec.Record state, int tick) {
        int index = bodyDataStore.getIndexByNetworkId(state.networkId);
        if (index == -1) {
            return;
        }

        PhysicsStateContainer container = bodyDataStore.getPhysicsStateContainer();
        if (tick <= container.stateTick.get(index)) {
            return;
        }

        bodyDataStore.getHistory().write(index, container);

        state.applyTo(container, index);
        container.stateTick.put(index, tick);
    }

    @Override
    public <T extends Body> T spawnBody(BodyType<T> bodyType, RVec3Arg position, QuatArg rotation, Consumer<T> configurator) {
        throw new UnsupportedOperationException("Bodies of a non-simulated client world can only be spawned by the server");
    }

    @Override
    public void removeBody(Body body, RemovalReason removalReason) {
        throw new UnsupportedOperationException("Bodies of a non-simulated client world can only be removed by the server");
    }

    @Override
    public int getJoltBodyId(UUID id) {
        return Jolt.cInvalidBodyId;
    }
}
