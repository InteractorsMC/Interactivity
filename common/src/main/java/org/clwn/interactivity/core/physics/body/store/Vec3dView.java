/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.body.store;

import org.clwn.interactivity.api.util.math.Vec3d;

import java.nio.DoubleBuffer;

public final class Vec3dView implements Vec3d {
    private final DoubleBuffer xBuffer;
    private final DoubleBuffer yBuffer;
    private final DoubleBuffer zBuffer;

    private final int index;

    public Vec3dView(
            DoubleBuffer xBuffer,
            DoubleBuffer yBuffer,
            DoubleBuffer zBuffer,
            int index
    ) {
        this.xBuffer = xBuffer;
        this.yBuffer = yBuffer;
        this.zBuffer = zBuffer;
        this.index = index;
    }

    @Override
    public double getX() {
        return xBuffer.get(index);
    }

    @Override
    public double getY() {
        return yBuffer.get(index);
    }

    @Override
    public double getZ() {
        return zBuffer.get(index);
    }
}
