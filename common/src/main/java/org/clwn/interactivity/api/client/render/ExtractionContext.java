/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.client.render;

import org.clwn.interactivity.api.util.math.Vec3d;

/**
 * Information available while a renderer copies the data it needs from a body into its render state.
 * <p>
 * Only valid during the call it was passed to.
 */
public interface ExtractionContext {
    /**
     * The pose the body is going to be drawn at, already sampled for this frame.
     * The renderer's {@code submit} is called with the pose stack already translated and rotated to it.
     */
    RenderPose getPose();

    /**
     * The position of the camera in world space.
     */
    Vec3d getCameraPosition();

    /**
     * The (fractional) physics tick that is on screen this frame.
     */
    double getRenderTick();
}

