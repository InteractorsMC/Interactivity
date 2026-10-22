/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.client;

import lombok.Getter;
import org.clwn.interactivity.core.client.render.RenderDispatcher;

public final class InteractivityCoreClient {
    @Getter
    private static RenderDispatcher renderDispatcher;

    private InteractivityCoreClient() {}

    public static void init() {
        renderDispatcher = new RenderDispatcher();
    }
}
