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
import lombok.Setter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.api.physics.PhysicsPipeline;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;
import org.clwn.interactivity.core.event.lifecycle.LevelLifecycleEvents;
import org.clwn.interactivity.core.event.lifecycle.LevelTickEvents;
import org.clwn.interactivity.core.network.packet.s2c.S2CPhysicsConfigPacket;
import org.clwn.interactivity.core.physics.world.PhysicsSimulationConfig;
import org.clwn.interactivity.core.physics.world.PhysicsWorldImpl;
import org.clwn.interactivity.core.physics.world.ServerPhysicsWorldImpl;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PhysicsPipelineImpl implements PhysicsPipeline {
    public static PhysicsPipelineImpl INSTANCE;
    private static final Map<Level, PhysicsWorldImpl> physicsWorlds = new ConcurrentHashMap<>();

    @Getter
    private final PhysicsLayerRegistryImpl physicsLayerRegistry;

    @Getter @Setter
    private PhysicsSimulationConfig physicsSimulationConfig;

    public PhysicsPipelineImpl() {
        LevelLifecycleEvents.LEVEL_LOAD_EVENT.subscribe((event) -> createServerPhysicsWorld(event.level()));
        LevelLifecycleEvents.LEVEL_UNLOAD_EVENT.subscribe((event) -> removePhysicsWorld(event.level()));
        LevelTickEvents.END.subscribe(this::publishPlayers);

        JoltPhysicsObject.startCleaner();
        Jolt.registerDefaultAllocator();

        Jolt.installDefaultTraceCallback();
        Jolt.installDefaultAssertCallback();

        boolean success = Jolt.newFactory();
        assert success;

        Jolt.registerTypes();

        this.physicsLayerRegistry = new PhysicsLayerRegistryImpl();
        this.physicsSimulationConfig = new PhysicsSimulationConfig(this.physicsLayerRegistry);
    }

    // todo: reset config on client on disconnect

    public void handleConfig(S2CPhysicsConfigPacket syncPacket) {
        this.physicsSimulationConfig = new PhysicsSimulationConfig(
                this.physicsLayerRegistry,
                syncPacket,
                this.physicsSimulationConfig.tempAllocatorSize()
        );
    }

    @Override
    public @Nullable PhysicsWorld getPhysicsWorld(Level level) {
        return physicsWorlds.get(level);
    }

    public PhysicsWorldImpl createServerPhysicsWorld(ServerLevel level) {
        PhysicsWorldImpl physicsWorld = new ServerPhysicsWorldImpl(level, physicsSimulationConfig);

        Interactivity.LOGGER.info("Initializing physics world for level: {}", level.dimension().location());
        registerWorld(level, physicsWorld);
        return physicsWorld;
    }

    /**
     * Registers an already constructed world for a level and initializes it.
     */
    public void registerWorld(Level level, PhysicsWorldImpl physicsWorld) {
        physicsWorlds.put(level, physicsWorld);
        physicsWorld.init();
    }

    public void removePhysicsWorld(Level level) {
        PhysicsWorldImpl physicsWorld = physicsWorlds.remove(level);
        if (physicsWorld == null) {
            return;
        }

        Interactivity.LOGGER.info("Removing physics world for level: {}", level.dimension().location());
        physicsWorld.close();
    }

    private void publishPlayers(LevelTickEvents.ServerLevelTickEvent event) {
        if (physicsWorlds.get(event.level()) instanceof ServerPhysicsWorldImpl serverWorld) {
            serverWorld.getNetworkDispatcher().publishPlayers(event.level());
        }
    }
}
