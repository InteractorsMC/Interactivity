/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.event;

import org.clwn.interactivity.api.physics.world.PhysicsWorld;

public abstract class PhysicsWorldLifecycleEvent extends PhysicsWorldEvent {
    public PhysicsWorldLifecycleEvent(PhysicsWorld physicsWorld) {
        super(physicsWorld);
    }

    public static final class Created extends PhysicsWorldLifecycleEvent {
        public Created(PhysicsWorld physicsWorld) {
            super(physicsWorld);
        }
    }

    public static final class Removed extends PhysicsWorldLifecycleEvent {
        public Removed(PhysicsWorld physicsWorld) {
            super(physicsWorld);
        }
    }
}
