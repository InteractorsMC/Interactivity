/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.world;

import com.github.stephengold.joltjni.BodyCreationSettings;
import com.github.stephengold.joltjni.BoxShapeSettings;
import com.github.stephengold.joltjni.RVec3;
import com.github.stephengold.joltjni.Vec3;
import com.github.stephengold.joltjni.enumerate.EActivation;
import com.github.stephengold.joltjni.enumerate.EMotionType;
import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import org.clwn.interactivity.api.physics.ObjectLayer;
import org.clwn.interactivity.api.physics.world.ServerPhysicsWorld;
import org.clwn.interactivity.core.config.InteractivityConfig;
import org.clwn.interactivity.core.event.Event;
import org.clwn.interactivity.core.event.lifecycle.BlockChangeEvents;
import org.clwn.interactivity.core.event.lifecycle.ChunkLifecycleEvents;
import org.clwn.interactivity.core.event.lifecycle.LevelTickEvents;
import org.clwn.interactivity.core.physics.body.ServerBodyManager;
import org.clwn.interactivity.core.physics.network.NetworkDispatcher;

import java.util.ArrayList;
import java.util.List;

public class ServerPhysicsWorldImpl extends SimulatedPhysicsWorldImpl implements ServerPhysicsWorld {
    @Getter
    protected final ServerLevel serverLevel;

    @Getter
    protected final ServerBodyManager bodyManager = new ServerBodyManager(this);

    @Getter
    protected final NetworkDispatcher networkDispatcher;

    private final List<Event.Subscription> subscriptions = new ArrayList<>();

    public ServerPhysicsWorldImpl(ServerLevel level, PhysicsSimulationConfig physicsSimulationConfig) {
        super(level, physicsSimulationConfig);
        this.serverLevel = level;
        this.networkDispatcher = new NetworkDispatcher(this);
    }

    @Override
    public void init() {
        super.init();

        subscriptions.add(ChunkLifecycleEvents.CHUNK_LOAD_EVENT.subscribe(event -> {
            if (event.level() == serverLevel) {
                onChunkLoad(event.chunk());
            }
        }));
        subscriptions.add(ChunkLifecycleEvents.CHUNK_UNLOAD_EVENT.subscribe(event -> {
            if (event.level() == serverLevel) {
                onChunkUnload(event.chunk());
            }
        }));
        subscriptions.add(BlockChangeEvents.BLOCK_CHANGED_EVENT.subscribe(event -> {
            if (event.level() == serverLevel) {
                onBlockChanged(event.chunk(), event.pos(), event.oldState(), event.newState());
            }
        }));
        subscriptions.add(LevelTickEvents.END.subscribe(event -> {
            if (event.level() == serverLevel) {
                onLevelTickEnd();
            }
        }));

        if (InteractivityConfig.devGroundPlane) {
            execute(this::createGroundPlane);
        }
    }

    @Override
    public void close() {
        subscriptions.forEach(Event.Subscription::close);
        subscriptions.clear();

        super.close();
    }

    @Override
    protected void afterSync(int tick) {
        networkDispatcher.update(tick, bodyManager.getBodyDataStore().getPhysicsStateContainer());
    }

    /**
     * Creates a static 64x64 floor so bodies have something to land on.
     * Its top face is at y = 0. It is a plain Jolt body, not a {@link org.clwn.interactivity.api.physics.body.Body},
     * so it is neither synchronized nor rendered.
     */
    private void createGroundPlane() {
        try (
                BoxShapeSettings shapeSettings = new BoxShapeSettings(new Vec3(32f, 0.5f, 32f));
                BodyCreationSettings bcs = new BodyCreationSettings()
        ) {
            bcs.setShape(shapeSettings.create().get());
            bcs.setPosition(new RVec3(0.0, -0.5, 0.0));
            bcs.setMotionType(EMotionType.Static);
            bcs.setObjectLayer(ObjectLayer.TERRAIN.getValue());

            getJoltBridge().getPhysicsSystem().getBodyInterface().createAndAddBody(bcs, EActivation.DontActivate);
        }
    }
}
