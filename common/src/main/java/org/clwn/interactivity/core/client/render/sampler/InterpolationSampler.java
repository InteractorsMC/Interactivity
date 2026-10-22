/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.client.render.sampler;

import net.minecraft.util.Mth;
import org.clwn.interactivity.core.client.render.MutableRenderPose;
import org.clwn.interactivity.core.physics.body.store.AbstractBodyDataStore;
import org.clwn.interactivity.core.physics.body.store.HistoricalPhysicsStateContainer;
import org.clwn.interactivity.core.physics.body.store.PhysicsStateContainer;
import org.joml.Quaternionf;

public class InterpolationSampler implements RenderSampler {
    private final Quaternionf target = new Quaternionf();

    @Override
    public void sample(AbstractBodyDataStore store, int index, double renderTick, MutableRenderPose out) {
        PhysicsStateContainer current = store.getPhysicsStateContainer();
        int currentTick = current.stateTick.get(index);

        HistoricalPhysicsStateContainer history = store.getHistory();
        PhysicsStateContainer previous = history.getData();
        int previousIndex = history.indexOf(index, currentTick - 1);
        int previousTick = previous.stateTick.get(previousIndex);

        if (previousTick == 0) {
            setFromState(current, index, out);
            return;
        }

        double delta = (renderTick - previousTick) / (currentTick - previousTick);
        delta = Math.clamp(delta, 0.0, 1.0);

        out.x = Mth.lerp((float) delta, previous.posX.get(previousIndex), current.posX.get(index));
        out.y = Mth.lerp((float) delta, previous.posY.get(previousIndex), current.posY.get(index));
        out.z = Mth.lerp((float) delta, previous.posZ.get(previousIndex), current.posZ.get(index));

        // JOML slerp follows the shortest arc
        out.rotation.set(previous.rotX.get(previousIndex), previous.rotY.get(previousIndex), previous.rotZ.get(previousIndex), previous.rotW.get(previousIndex));
        target.set(current.rotX.get(index), current.rotY.get(index), current.rotZ.get(index), current.rotW.get(index));
        out.rotation.slerp(target, (float) delta);
    }

    static void setFromState(PhysicsStateContainer state, int index, MutableRenderPose out) {
        out.x = state.posX.get(index);
        out.y = state.posY.get(index);
        out.z = state.posZ.get(index);
        out.rotation.set(state.rotX.get(index), state.rotY.get(index), state.rotZ.get(index), state.rotW.get(index));
    }
}
