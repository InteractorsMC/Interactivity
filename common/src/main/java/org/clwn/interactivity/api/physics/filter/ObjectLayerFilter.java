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
import org.clwn.interactivity.api.physics.ObjectLayer;

@AllArgsConstructor
@Getter
// filters worked for me really strange before, so creating a jolt-jni might make sense once I test it
public enum ObjectLayerFilter {
    ALL(new com.github.stephengold.joltjni.ObjectLayerFilter()),

    // todo: better name, maybe?
    SIMULATED(new com.github.stephengold.joltjni.ObjectLayerFilter() {
        @Override
        public boolean shouldCollide(int objectLayer) {
            return objectLayer != ObjectLayer.TERRAIN.getValue() && objectLayer != ObjectLayer.GHOST.getValue();
        }
    }),

    MOVING(new com.github.stephengold.joltjni.ObjectLayerFilter() {
        @Override
        public boolean shouldCollide(int objectLayer) {
            return objectLayer == ObjectLayer.MOVING.getValue();
        }
    }),

    COLLIDING(new com.github.stephengold.joltjni.ObjectLayerFilter() {
        @Override
        public boolean shouldCollide(int objectLayer) {
            return objectLayer != ObjectLayer.GHOST.getValue();
        }
    });

    private final com.github.stephengold.joltjni.ObjectLayerFilter filter;
}
