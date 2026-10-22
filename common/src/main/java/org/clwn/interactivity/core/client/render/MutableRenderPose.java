/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.client.render;

import org.clwn.interactivity.api.client.render.RenderPose;
import org.clwn.interactivity.api.util.math.Quat;
import org.clwn.interactivity.api.util.math.Vec3d;
import org.joml.Quaternionf;

/**
 * Reusable {@link RenderPose}. The samplers write into it, so sampling allocates nothing.
 */
public final class MutableRenderPose implements RenderPose {
    public double x;
    public double y;
    public double z;
    public final Quaternionf rotation = new Quaternionf();

    private final Vec3d positionView = new Vec3d() {
        @Override
        public double getX() {
            return x;
        }

        @Override
        public double getY() {
            return y;
        }

        @Override
        public double getZ() {
            return z;
        }
    };

    private final Quat rotationView = new Quat() {
        @Override
        public float getX() {
            return rotation.x;
        }

        @Override
        public float getY() {
            return rotation.y;
        }

        @Override
        public float getZ() {
            return rotation.z;
        }

        @Override
        public float getW() {
            return rotation.w;
        }
    };

    @Override
    public Vec3d getPosition() {
        return positionView;
    }

    @Override
    public Quat getRotation() {
        return rotationView;
    }
}
