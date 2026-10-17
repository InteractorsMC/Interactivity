/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.world;

import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import org.clwn.interactivity.api.physics.world.ServerPhysicsWorld;
import org.clwn.interactivity.core.physics.PhysicsLayerRegistryImpl;
import org.clwn.interactivity.core.physics.body.ServerBodyManager;

public class ServerPhysicsWorldImpl extends PhysicsWorldImpl implements ServerPhysicsWorld {
    @Getter
    protected final ServerLevel serverLevel;

    @Getter
    protected final ServerBodyManager bodyManager = new ServerBodyManager(this);

    public ServerPhysicsWorldImpl(ServerLevel level, PhysicsLayerRegistryImpl physicsLayerRegistry) {
        super(level, physicsLayerRegistry);
        this.serverLevel = level;
    }
}