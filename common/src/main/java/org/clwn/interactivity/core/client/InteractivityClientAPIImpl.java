/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.client;

import org.clwn.interactivity.api.client.InteractivityClientAPI;
import org.clwn.interactivity.api.client.physics.ClientPhysicsPipeline;
import org.clwn.interactivity.api.client.physics.render.BodyRendererRegistry;
import org.clwn.interactivity.core.client.physics.ClientPhysicsPipelineImpl;
import org.clwn.interactivity.core.client.physics.render.BodyRendererRegistryImpl;
import org.jetbrains.annotations.Nullable;

public class InteractivityClientAPIImpl implements InteractivityClientAPI {
    public static final InteractivityClientAPIImpl INSTANCE = new InteractivityClientAPIImpl();

    private InteractivityClientAPIImpl() {

    }

    @Override
    public BodyRendererRegistry getBodyRendererRegistry() {
        return BodyRendererRegistryImpl.INSTANCE;
    }

    @Override
    public @Nullable ClientPhysicsPipeline getClientPhysicsPipeline() {
        return ClientPhysicsPipelineImpl.INSTANCE;
    }
}
