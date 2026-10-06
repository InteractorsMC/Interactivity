/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics;

import com.github.stephengold.joltjni.Jolt;
import com.github.stephengold.joltjni.JoltPhysicsObject;
import lombok.Getter;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.clwn.interactivity.api.physics.PhysicsPipeline;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;
import org.clwn.interactivity.core.client.physics.world.ClientPhysicsWorldImpl;
import org.clwn.interactivity.core.event.lifecycle.LevelLifecycleEvents;
import org.clwn.interactivity.core.physics.world.PhysicsWorldImpl;
import org.clwn.interactivity.core.physics.world.ServerPhysicsWorldImpl;
import org.clwn.interactivity.core.util.platform.Services;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PhysicsPipelineImpl implements PhysicsPipeline {
    public static @Nullable PhysicsPipelineImpl INSTANCE;
    private static final Map<Level, PhysicsWorldImpl> physicsWorlds = new ConcurrentHashMap<>();

    @Getter
    private final PhysicsLayerRegistryImpl physicsLayerRegistry;

    public PhysicsPipelineImpl() {
        LevelLifecycleEvents.LEVEL_LOAD_EVENT.subscribe((event) -> createPhysicsWorld(event.level()));
        LevelLifecycleEvents.LEVEL_UNLOAD_EVENT.subscribe((event) -> removePhysicsWorld(event.level()));

        JoltPhysicsObject.startCleaner();
        Jolt.registerDefaultAllocator();

        Jolt.installDefaultTraceCallback();
        Jolt.installDefaultAssertCallback();

        Jolt.registerTypes();

        this.physicsLayerRegistry = new PhysicsLayerRegistryImpl();
    }

    @Override
    public PhysicsWorld getPhysicsWorld(Level level) {
        return physicsWorlds.get(level);
    }

    public PhysicsWorldImpl createPhysicsWorld(Level level) {
        PhysicsWorldImpl physicsWorld = null;

        if (Services.PLATFORM.isClientEnvironment()) {
            if (level instanceof ClientLevel clientLevel) { // todo: put it outside of common code to prevent client classes loading on server
                physicsWorld = new ClientPhysicsWorldImpl(clientLevel, physicsLayerRegistry);
            }
        }

        if (physicsWorld == null && level instanceof ServerLevel serverLevel) {
            physicsWorld = new ServerPhysicsWorldImpl(serverLevel, physicsLayerRegistry);
        } else {
            throw new IllegalArgumentException("Unsupported level type: " + level.getClass().getName());
        }

        physicsWorlds.put(level, physicsWorld);
        physicsWorld.init();
        return physicsWorld;
    }

    public void removePhysicsWorld(Level level) {
        PhysicsWorldImpl physicsWorld = physicsWorlds.remove(level);
        physicsWorld.close();
    }
}
