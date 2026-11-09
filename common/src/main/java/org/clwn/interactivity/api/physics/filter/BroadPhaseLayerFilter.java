/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clwn.interactivity.api.physics.BroadPhaseLayer;

@SuppressWarnings("resource") // system will clean up jolt-jni resources on exit, and they shouldn't be closed in lifetime
@Getter
@AllArgsConstructor
// todo: no natives client will most likely fail to load due to missing shared object;
//  if physics system for estimated world will be eventually be used, we can ignore that.
public enum BroadPhaseLayerFilter {
    /**
     * A standard broad-phase filter that permits collision with all layers.
     */
    ALL(new com.github.stephengold.joltjni.BroadPhaseLayerFilter()),

    /**
     * A standard broad-phase filter that ignores the NON_MOVING layer.
     */
    IGNORE_NON_MOVING(new com.github.stephengold.joltjni.BroadPhaseLayerFilter() {
        @Override
        public boolean shouldCollide(int broadPhaseLayer) {
            return broadPhaseLayer != BroadPhaseLayer.NON_MOVING.getValue();
        }
    });

    private final com.github.stephengold.joltjni.BroadPhaseLayerFilter filter;
}
