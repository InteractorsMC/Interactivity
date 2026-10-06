/*
 * Copyright (c) 2025 xI-Mx-Ix
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.client.physics;

public interface PhysicsLayerRegistry {
    // Object Layers
    /**
     * Static and kinematic bodies controlled by the application code
     */
    short NON_MOVING = 0;

    /**
     * Dynamic bodies simulated by the physics engine
     */
    short MOVING = 1;

    /**
     * Terrain geometry
     */
    short TERRAIN = 2;

    /**
     * Doesn't collide with anything, used for triggers and sensors
     */
    short GHOST = 3;

    /**
     * Maximum number of pre-allocated object layers to allow dynamic runtime additions
     */
    short MAX_OBJECT_LAYERS = 64;

    // Broad-phase layers

    /**
     * Broad-phase layer for non-moving objects
     */
    short BP_NON_MOVING = 0;

    /**
     * Broad-phase layer for moving objects
     */
    short BP_MOVING = 1;

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
    short claimLayer();

    /**
     * Configures whether two object layers are permitted to collide.
     * This can be updated at runtime after initialization.
     *
     * @param layer1  The first object layer.
     * @param layer2  The second object layer.
     * @param enabled True if collision should be enabled, false otherwise.
     */
    void setCollision(short layer1, short layer2, boolean enabled);

    /**
     * Assigns an object layer to a specific broad-phase layer.
     * This can be updated at runtime after initialization.
     *
     * @param objectLayer     The object layer ID.
     * @param broadPhaseLayer The broad-phase layer ID.
     */
    void setBroadPhaseMapping(short objectLayer, short broadPhaseLayer);
}
