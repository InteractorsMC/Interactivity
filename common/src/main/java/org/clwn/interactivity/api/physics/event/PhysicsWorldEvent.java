/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.event;

import lombok.Getter;
import org.clwn.interactivity.api.event.Event;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;

public abstract class PhysicsWorldEvent extends Event {
    @Getter
    protected final PhysicsWorld physicsWorld;

    public PhysicsWorldEvent(PhysicsWorld physicsWorld) {
        this.physicsWorld = physicsWorld;
    }
}
