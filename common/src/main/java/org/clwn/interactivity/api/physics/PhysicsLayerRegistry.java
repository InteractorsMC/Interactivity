/*
 * Copyright (c) 2025-2026 xI-Mx-Ix
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics;

public interface PhysicsLayerRegistry {
    /**
     * Maximum number of pre-allocated object layers to allow dynamic runtime additions
     */
    short MAX_OBJECT_LAYERS = 64;

    // Broad-phase layers

    /**
     * Total number of broad-phase layers
     */
    short NUM_BROAD_PHASE_LAYERS = 2;

    /**
     * Reserves a unique object layer ID from the pre-allocated pool.
     * <p>
     *     By default, layer will be mapped to NON-MOVING broad-phase layer.
     * </p>
     *
     * @return A new unique layer ID.
     * @throws IllegalStateException If the maximum number of layers has been exceeded.
     */
    int claimLayer();

    /**
     * Configures whether two object layers are permitted to collide.
     * This can be updated at runtime after initialization.
     *
     * @param layer1  The first object layer.
     * @param layer2  The second object layer.
     * @param enabled True if collision should be enabled, false otherwise.
     */
    void setCollision(int layer1, int layer2, boolean enabled);

    default void setBroadPhaseMapping(int objectLayer, BroadPhaseLayer broadPhaseLayer) {
        setBroadPhaseMapping(objectLayer, broadPhaseLayer.getValue());
    }

    /**
     * Assigns an object layer to a specific broad-phase layer.
     * This can be updated at runtime after initialization.
     *
     * @param objectLayer     The object layer ID.
     * @param broadPhaseLayer The broad-phase layer ID.
     */
    void setBroadPhaseMapping(int objectLayer, int broadPhaseLayer);
}
