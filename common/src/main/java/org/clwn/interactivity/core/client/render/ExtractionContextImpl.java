/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.client.render;

import org.clwn.interactivity.api.client.render.ExtractionContext;
import org.clwn.interactivity.api.client.render.RenderPose;
import org.clwn.interactivity.api.util.math.Vec3d;

final class ExtractionContextImpl implements ExtractionContext {
    private MutableRenderPose pose;
    private double renderTick;
    private double cameraX;
    private double cameraY;
    private double cameraZ;

    private final Vec3d cameraPosition = new Vec3d() {
        @Override
        public double getX() {
            return cameraX;
        }

        @Override
        public double getY() {
            return cameraY;
        }

        @Override
        public double getZ() {
            return cameraZ;
        }
    };

    void beginFrame(double cameraX, double cameraY, double cameraZ, double renderTick) {
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.cameraZ = cameraZ;
        this.renderTick = renderTick;
    }

    void setPose(MutableRenderPose pose) {
        this.pose = pose;
    }

    @Override
    public RenderPose getPose() {
        return pose;
    }

    @Override
    public Vec3d getCameraPosition() {
        return cameraPosition;
    }

    @Override
    public double getRenderTick() {
        return renderTick;
    }
}
