/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.client;

import lombok.Getter;
import org.clwn.interactivity.api.client.physics.render.BodyRenderer;
import org.clwn.interactivity.api.client.physics.render.RegisterBodyRenderersEvent;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.clwn.interactivity.core.client.physics.render.BodyRendererRegistryImpl;
import org.clwn.interactivity.core.client.render.RenderDispatcher;
import org.clwn.interactivity.core.event.external.EventBusImpl;

public final class InteractivityCoreClient {
    @Getter
    private static RenderDispatcher renderDispatcher;

    private InteractivityCoreClient() {}

    public static void init() {
        renderDispatcher = new RenderDispatcher();
    }

    public static void registerContent() {
        EventBusImpl.INSTANCE.publishEvent(new RegisterBodyRenderersEvent() {
            @Override
            public <B extends Body> void register(BodyType<B> bodyType, BodyRenderer<?> renderer) {
                BodyRendererRegistryImpl.INSTANCE.register(bodyType, renderer);
            }
        });
        BodyRendererRegistryImpl.INSTANCE.freeze();
    }
}
