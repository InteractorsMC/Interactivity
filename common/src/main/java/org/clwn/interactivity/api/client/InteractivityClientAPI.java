/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.client;

import org.clwn.interactivity.api.client.physics.render.BodyRendererRegistry;
import org.clwn.interactivity.core.client.InteractivityClientAPIImpl;

public interface InteractivityClientAPI {
    static InteractivityClientAPI getInstance() {
        return InteractivityClientAPIImpl.INSTANCE;
    }

    BodyRendererRegistry getBodyRendererRegistry();
}
