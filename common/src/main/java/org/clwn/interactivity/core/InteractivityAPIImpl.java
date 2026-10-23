/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core;

import org.clwn.interactivity.api.InteractivityAPI;
import org.clwn.interactivity.api.network.NetworkManager;
import org.clwn.interactivity.api.physics.PhysicsPipeline;
import org.clwn.interactivity.api.physics.body.BodyTypeRegistry;
import org.clwn.interactivity.api.physics.event.EventBus;
import org.clwn.interactivity.core.physics.event.EventBusImpl;
import org.clwn.interactivity.core.network.NetworkManagerImpl;
import org.clwn.interactivity.core.physics.PhysicsPipelineImpl;
import org.clwn.interactivity.core.physics.body.type.BodyTypeRegistryImpl;

public class InteractivityAPIImpl implements InteractivityAPI {
    public static final InteractivityAPI INSTANCE = new InteractivityAPIImpl();

    private InteractivityAPIImpl() {
    }


    @Override
    public PhysicsPipeline getPhysicsPipeline() {
        return PhysicsPipelineImpl.INSTANCE;
    }

    @Override
    public BodyTypeRegistry getBodyTypeRegistry() {
        return BodyTypeRegistryImpl.INSTANCE;
    }

    @Override
    public NetworkManager getNetworkManager() {
        return NetworkManagerImpl.INSTANCE;
    }

    @Override
    public EventBus getEventBus() {
        return EventBusImpl.INSTANCE;
    }
}
