/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.body;

import lombok.Getter;
import lombok.Setter;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;

import java.util.UUID;

public abstract class Body {
    private final PhysicsWorld world;
    private final UUID uuid;
    private final BodyType<? extends Body> type;

    @Getter @Setter
    private int physicsId;

    protected Body(BodyType<? extends Body> type, PhysicsWorld world, UUID uuid) {
        this.world = world;
        this.uuid = uuid;
        this.type = type;
    }

    public final PhysicsWorld getWorld() {
        return world;
    }

    public final UUID getUuid() {
        return uuid;
    }

    public final BodyType<? extends Body> getType() {
        return type;
    }
}
