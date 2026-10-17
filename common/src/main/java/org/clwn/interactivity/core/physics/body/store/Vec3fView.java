/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.body.store;

import org.clwn.interactivity.api.util.math.Vec3f;

import java.nio.FloatBuffer;

public final class Vec3fView implements Vec3f {
    private final FloatBuffer xBuffer;
    private final FloatBuffer yBuffer;
    private final FloatBuffer zBuffer;

    private final int index;

    public Vec3fView(
            FloatBuffer xBuffer,
            FloatBuffer yBuffer,
            FloatBuffer zBuffer,
            int index
    ) {
        this.xBuffer = xBuffer;
        this.yBuffer = yBuffer;
        this.zBuffer = zBuffer;
        this.index = index;
    }

    @Override
    public float getX() {
        return xBuffer.get(index);
    }

    @Override
    public float getY() {
        return yBuffer.get(index);
    }

    @Override
    public float getZ() {
        return zBuffer.get(index);
    }
}
