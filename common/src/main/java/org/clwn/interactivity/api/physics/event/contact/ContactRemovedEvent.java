/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.event.contact;

import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;
import org.jetbrains.annotations.Nullable;

/**
 * Also sent when a body falls asleep or the other body was removed. Jolt can't access the bodies at this point,
 * so there is no contact point.
 */
public class ContactRemovedEvent extends ContactEvent {
    public ContactRemovedEvent(PhysicsWorld physicsWorld, Body body, @Nullable Body other, boolean terrain) {
        super(physicsWorld, body, other, terrain);
    }
}
