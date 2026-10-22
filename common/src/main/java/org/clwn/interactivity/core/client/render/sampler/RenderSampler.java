/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.client.render.sampler;

import org.clwn.interactivity.core.client.render.MutableRenderPose;
import org.clwn.interactivity.core.physics.body.store.AbstractBodyDataStore;

/**
 * Chooses the pose a body is drawn at, from the states received from the server.
 */
public interface RenderSampler {
    /**
     * @param renderTick the (fractional) server physics tick that should be on screen
     * @param out        receives the pose
     */
    void sample(AbstractBodyDataStore store, int index, double renderTick, MutableRenderPose out);
}
