/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.client.render.sampler;

import org.clwn.interactivity.core.client.render.MutableRenderPose;
import org.clwn.interactivity.core.config.InteractivityConfig;
import org.clwn.interactivity.core.physics.PhysicsPipelineImpl;
import org.clwn.interactivity.core.physics.body.store.AbstractBodyDataStore;
import org.clwn.interactivity.core.physics.body.store.PhysicsStateContainer;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ExtrapolationSampler extends InterpolationSampler {
    private final Quaternionf delta = new Quaternionf();
    private final Vector3f axis = new Vector3f();

    @Override
    public void sample(AbstractBodyDataStore store, int index, double renderTick, MutableRenderPose out) {
        PhysicsStateContainer current = store.getPhysicsStateContainer();
        int currentTick = current.stateTick.get(index);

        if (renderTick <= currentTick || current.isActive.get(index) == 0) {
            super.sample(store, index, renderTick, out);
            return;
        }

        double seconds = Math.min(
                (renderTick - currentTick) / PhysicsPipelineImpl.INSTANCE.getPhysicsSimulationConfig().tickRate(),
                InteractivityConfig.maxExtrapolationMs / 1000.0
        );

        out.x = current.posX.get(index) + current.velX.get(index) * seconds;
        out.y = current.posY.get(index) + current.velY.get(index) * seconds;
        out.z = current.posZ.get(index) + current.velZ.get(index) * seconds;

        // Angular velocity is in world space, so the rotation delta is applied in front of the current rotation
        axis.set(current.angVelX.get(index), current.angVelY.get(index), current.angVelZ.get(index));
        float speed = axis.length();
        if (speed > 1e-6f) {
            axis.div(speed);
            delta.fromAxisAngleRad(axis, (float) (speed * seconds));
            out.rotation.set(delta).mul(current.rotX.get(index), current.rotY.get(index), current.rotZ.get(index), current.rotW.get(index));
        } else {
            out.rotation.set(current.rotX.get(index), current.rotY.get(index), current.rotZ.get(index), current.rotW.get(index));
        }
    }
}
