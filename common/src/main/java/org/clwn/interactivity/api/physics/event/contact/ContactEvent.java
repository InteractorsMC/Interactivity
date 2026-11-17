/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.event.contact;

import lombok.Getter;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.event.body.BodyEvent;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;
import org.jetbrains.annotations.Nullable;

/**
 * A contact of the body, received with {@link org.clwn.interactivity.api.event.ContactNotifierEvent}.
 */
public abstract class ContactEvent extends BodyEvent {
    /**
     * The touched body, {@code null} for terrain and removed bodies.
     */
    @Getter
    protected final @Nullable Body other;

    @Getter
    protected final boolean terrain;

    protected ContactEvent(PhysicsWorld physicsWorld, Body body, @Nullable Body other, boolean terrain) {
        super(physicsWorld, body);
        this.other = other;
        this.terrain = terrain;
    }
}
