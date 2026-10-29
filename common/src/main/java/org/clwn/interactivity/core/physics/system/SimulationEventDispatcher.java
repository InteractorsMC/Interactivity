/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.system;

import org.clwn.interactivity.api.physics.event.PhysicsTickEvent;
import org.clwn.interactivity.core.event.external.EventBusImpl;
import org.clwn.interactivity.core.physics.world.PhysicsWorldImpl;

/**
 * Dispatches all physics world events to the appropriate listeners.
 */
public final class SimulationEventDispatcher {
    private SimulationEventDispatcher() {}

    public static void prePhysicsTick(PhysicsWorldImpl physicsWorld, float deltaTime) {
        EventBusImpl.INSTANCE.publishEvent(new PhysicsTickEvent.PrePhysicsTickEvent(physicsWorld, deltaTime));
    }

    public static void postPhysicsTick(PhysicsWorldImpl physicsWorld, float deltaTime) {
        EventBusImpl.INSTANCE.publishEvent(new PhysicsTickEvent.PostPhysicsTickEvent(physicsWorld, deltaTime));
    }
}
