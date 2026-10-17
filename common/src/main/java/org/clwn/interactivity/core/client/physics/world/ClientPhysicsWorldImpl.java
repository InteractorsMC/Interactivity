/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.client.physics.world;

import lombok.Getter;
import net.minecraft.client.multiplayer.ClientLevel;
import org.clwn.interactivity.api.client.physics.world.ClientPhysicsWorld;
import org.clwn.interactivity.core.physics.PhysicsLayerRegistryImpl;
import org.clwn.interactivity.core.physics.body.AbstractBodyManager;
import org.clwn.interactivity.core.physics.world.PhysicsWorldImpl;

public class ClientPhysicsWorldImpl extends PhysicsWorldImpl implements ClientPhysicsWorld {
    @Getter
    ClientLevel clientLevel;

    public ClientPhysicsWorldImpl(ClientLevel level, PhysicsLayerRegistryImpl physicsLayerRegistry) {
        super(level, physicsLayerRegistry);
        this.clientLevel = level;
    }

    @Override
    public AbstractBodyManager getBodyManager() {
        // todo
        return null;
    }
}
