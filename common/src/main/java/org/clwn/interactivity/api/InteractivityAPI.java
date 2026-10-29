/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api;

import org.clwn.interactivity.api.network.NetworkManager;
import org.clwn.interactivity.api.physics.PhysicsPipeline;
import org.clwn.interactivity.api.physics.behavior.BehaviorRegistry;
import org.clwn.interactivity.api.physics.body.BodyTypeRegistry;
import org.clwn.interactivity.api.event.EventBus;
import org.clwn.interactivity.core.InteractivityAPIImpl;

public interface InteractivityAPI {
    static InteractivityAPI getInstance() {
        return InteractivityAPIImpl.INSTANCE;
    }

    PhysicsPipeline getPhysicsPipeline();

    BehaviorRegistry getBehaviorRegistry();
    BodyTypeRegistry getBodyTypeRegistry();

    NetworkManager getNetworkManager();

    EventBus getEventBus();
}
