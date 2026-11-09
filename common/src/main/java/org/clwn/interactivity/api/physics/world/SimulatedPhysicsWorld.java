/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.world;

import org.clwn.interactivity.api.physics.util.intersection.ApproximateIntersector;
import org.clwn.interactivity.api.physics.util.intersection.Intersector;
import org.clwn.interactivity.api.physics.util.intersection.PreciseIntersector;

/**
 * A {@link PhysicsWorld} that runs the Jolt simulation.
 */
public interface SimulatedPhysicsWorld extends PhysicsWorld {
    void pauseSimulation();

    void continueSimulation();

    /**
     * Checks if simulation is rolling back
     * @return {@code true} if simulation is rolling back, {@code false} otherwise
     */
    boolean isRollingBack();

    JoltBridge getJoltBridge();

    @Override
    default boolean isSimulated() {
        return true;
    }

    /**
     * Performs intersections checks with a higher accuracy, but slower than {@link #getApproximateIntersector()}.
     */
    PreciseIntersector getPreciseIntersector();

    /**
     * Performs intersections checks with a lower accuracy, but faster than {@link #getPreciseIntersector()}.
     * <p>
     *     Can cause false positives, but never false negatives.
     * </p>
     */
    ApproximateIntersector getApproximateIntersector();
}
