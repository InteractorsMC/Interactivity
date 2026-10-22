/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.client.physics;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;
import org.clwn.interactivity.core.client.event.lifecycle.ClientLevelLifecycleEvents;
import org.clwn.interactivity.core.client.physics.world.ClientPhysicsWorldInternal;
import org.clwn.interactivity.core.client.physics.world.EstimatedClientPhysicsWorldImpl;
import org.clwn.interactivity.core.physics.PhysicsPipelineImpl;

public final class ClientPhysicsPipelineHelper {
    private final PhysicsPipelineImpl physicsPipeline;

    public ClientPhysicsPipelineHelper(PhysicsPipelineImpl physicsPipeline) {
        this.physicsPipeline = physicsPipeline;

        ClientLevelLifecycleEvents.LEVEL_LOAD_EVENT.subscribe(event -> createClientPhysicsWorld(event.level()));
        ClientLevelLifecycleEvents.LEVEL_UNLOAD_EVENT.subscribe(event -> physicsPipeline.removePhysicsWorld(event.level()));
    }

    private void createClientPhysicsWorld(ClientLevel level) {
        Interactivity.LOGGER.info("Initializing client physics world for level: {}", level.dimension().location());

        // todo: use the simulated world when client-side prediction is enabled and implemented
        EstimatedClientPhysicsWorldImpl world = new EstimatedClientPhysicsWorldImpl(level);
        physicsPipeline.registerWorld(level, world);
    }

    public static ClientPhysicsWorldInternal getCurrentPhysicsWorld() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }

        PhysicsWorld world = PhysicsPipelineImpl.INSTANCE.getPhysicsWorld(level);
        return world instanceof ClientPhysicsWorldInternal clientPhysicsWorld ? clientPhysicsWorld : null;
    }
}
