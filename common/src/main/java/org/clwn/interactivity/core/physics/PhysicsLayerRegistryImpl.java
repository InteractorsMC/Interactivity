/*
 * Copyright (c) 2025-2026 xI-Mx-Ix
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics;

import com.github.stephengold.joltjni.*;
import lombok.Getter;
import org.clwn.interactivity.api.physics.BroadPhaseLayer;
import org.clwn.interactivity.api.physics.ObjectLayer;
import org.clwn.interactivity.api.physics.PhysicsLayerRegistry;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Defines object and broad-phase layers for the Jolt physics simulation.
 */
public final class PhysicsLayerRegistryImpl implements PhysicsLayerRegistry {
    private final AtomicInteger nextAvailableLayer = new AtomicInteger(3);

    @Getter
    private final BroadPhaseLayerInterfaceTable broadPhaseLayerInterface;

    @Getter
    private final ObjectVsBroadPhaseLayerFilter objectVsBroadPhaseLayerFilter;

    @Getter
    private final ObjectLayerPairFilterTable objectLayerPairFilter;

    /**
     * Initializes all collision filtering interfaces.
     *
     * <p>This method must be called exactly once during physics world setup.
     * It defines which object layers can collide with each other and how
     * object layers are mapped to broad-phase layers.</p>
     */
    public PhysicsLayerRegistryImpl() {

        /* -------- Object Layer Pair Filter --------
         * Defines which pairs of object layers are allowed to collide.
         * We initialize with MAX_OBJECT_LAYERS to allow dynamic configuration.
         */
        objectLayerPairFilter = new ObjectLayerPairFilterTable(MAX_OBJECT_LAYERS);

        // Non-moving objects never collide with each other
        objectLayerPairFilter.disableCollision(ObjectLayer.NON_MOVING.getValue(), ObjectLayer.NON_MOVING.getValue());

        // Moving objects collide with everything
        objectLayerPairFilter.enableCollision(ObjectLayer.MOVING.getValue(), ObjectLayer.NON_MOVING.getValue());
        objectLayerPairFilter.enableCollision(ObjectLayer.MOVING.getValue(), ObjectLayer.MOVING.getValue());
        objectLayerPairFilter.enableCollision(ObjectLayer.MOVING.getValue(), ObjectLayer.TERRAIN.getValue());

        // Terrain only collides with moving objects
        objectLayerPairFilter.disableCollision(ObjectLayer.TERRAIN.getValue(), ObjectLayer.TERRAIN.getValue());
        objectLayerPairFilter.disableCollision(ObjectLayer.TERRAIN.getValue(), ObjectLayer.NON_MOVING.getValue());

        // Ghost
        objectLayerPairFilter.disableCollision(ObjectLayer.GHOST.getValue(), ObjectLayer.NON_MOVING.getValue());
        objectLayerPairFilter.disableCollision(ObjectLayer.GHOST.getValue(), ObjectLayer.TERRAIN.getValue());
        objectLayerPairFilter.disableCollision(ObjectLayer.GHOST.getValue(), ObjectLayer.MOVING.getValue());
        objectLayerPairFilter.disableCollision(ObjectLayer.GHOST.getValue(), ObjectLayer.GHOST.getValue());

        /* -------- Broad Phase Layer Interface --------
         * Maps object layers to broad-phase layers.
         */
        broadPhaseLayerInterface =
                new BroadPhaseLayerInterfaceTable(
                        MAX_OBJECT_LAYERS,
                        NUM_BROAD_PHASE_LAYERS
                );

        broadPhaseLayerInterface.mapObjectToBroadPhaseLayer(ObjectLayer.NON_MOVING.getValue(), BroadPhaseLayer.NON_MOVING.getValue());
        broadPhaseLayerInterface.mapObjectToBroadPhaseLayer(ObjectLayer.TERRAIN.getValue(), BroadPhaseLayer.NON_MOVING.getValue());
        broadPhaseLayerInterface.mapObjectToBroadPhaseLayer(ObjectLayer.MOVING.getValue(), BroadPhaseLayer.MOVING.getValue());
        broadPhaseLayerInterface.mapObjectToBroadPhaseLayer(ObjectLayer.GHOST.getValue(), BroadPhaseLayer.MOVING.getValue());

        // Initialize remaining slots to non-moving broad-phase by default
        for (int i = ObjectLayer.values().length; i < MAX_OBJECT_LAYERS; i++) {
            broadPhaseLayerInterface.mapObjectToBroadPhaseLayer(i, BroadPhaseLayer.NON_MOVING.getValue());
        }

        /* -------- Object vs Broad Phase Layer Filter --------
         * Combines object-layer and broad-phase-layer filtering.
         */
        objectVsBroadPhaseLayerFilter =
                new ObjectVsBroadPhaseLayerFilterTable(
                        broadPhaseLayerInterface,
                        NUM_BROAD_PHASE_LAYERS,
                        objectLayerPairFilter,
                        MAX_OBJECT_LAYERS
                );
    }

    @Override
    public int claimLayer() {
        int id = nextAvailableLayer.getAndIncrement();
        if (id >= MAX_OBJECT_LAYERS) {
            throw new IllegalStateException("Maximum pre-allocated physics layers reached: " + MAX_OBJECT_LAYERS);
        }
        return (int) id;
    }

    @Override
    public void setCollision(int layer1, int layer2, boolean enabled) {
        if (enabled) {
            objectLayerPairFilter.enableCollision(layer1, layer2);
        } else {
            objectLayerPairFilter.disableCollision(layer1, layer2);
        }
    }

    @Override
    public void setBroadPhaseMapping(int objectLayer, int broadPhaseLayer) {
        broadPhaseLayerInterface.mapObjectToBroadPhaseLayer(objectLayer, broadPhaseLayer);
    }

    /**
     * Releases all native resources used by the layer interfaces.
     *
     * <p>This method should be called during application shutdown
     * to avoid native memory leaks.</p>
     */
    public void shutdown() {
        objectVsBroadPhaseLayerFilter.close();

        objectLayerPairFilter.close();

        broadPhaseLayerInterface.close();
    }
}