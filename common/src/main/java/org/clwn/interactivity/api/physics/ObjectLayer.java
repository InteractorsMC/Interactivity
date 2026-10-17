/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics;

import lombok.Getter;

/**
 * Represents a layer in the physics simulation.
 * <p>
 *     Layers are used to categorize objects in the simulation, allowing for selective collision detection and interaction.
 * </p>
 * <p>
 *     For creating own Object Layers see {@link PhysicsLayerRegistry}
 * </p>
 */
public enum ObjectLayer {
    NON_MOVING(0),
    MOVING(1),
    TERRAIN(2),
    GHOST(3);

    @Getter
    private final int value;

    ObjectLayer(int value) {
        this.value = value;
    }
}
