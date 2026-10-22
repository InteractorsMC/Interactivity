/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.client.render;

import org.clwn.interactivity.api.util.math.Quat;
import org.clwn.interactivity.api.util.math.Vec3d;

/**
 * The pose a body is drawn at this frame, in world space.
 * <p>
 * Read-only and only valid during the call it was passed to.
 */
public interface RenderPose {
    Vec3d getPosition();

    Quat getRotation();
}
