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

// todo: body types and renderers registry could be refactored as events, and current Event could be changed to PhysicsWorldEvent, and event will be moved to api.event and core.event.external. I'm really not sure about this, but I will take a look at that when I will have time.
// todo: also might be useful to add CustomEvent for addons which can be toggled for listeners by `permitCustomEvents`
public abstract class PhysicsWorldEvent extends Event {
    @Getter
    protected final PhysicsWorld physicsWorld;

    public PhysicsWorldEvent(PhysicsWorld physicsWorld) {
        this.physicsWorld = physicsWorld;
    }
}
