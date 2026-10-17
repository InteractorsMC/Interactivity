/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.body;

import lombok.Getter;
import org.clwn.interactivity.core.physics.body.store.ServerBodyDataStore;
import org.clwn.interactivity.core.physics.world.PhysicsWorldImpl;


public class ServerBodyManager extends AbstractBodyManager {
    @Getter
    protected ServerBodyDataStore bodyDataStore = new ServerBodyDataStore();

    public ServerBodyManager(PhysicsWorldImpl physicsWorld) {
        super(physicsWorld);
    }
}
