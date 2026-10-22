/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.client.physics;

import lombok.Getter;
import org.clwn.interactivity.core.physics.PhysicsPipelineImpl;

public class ClientPhysicsClock {
    private static final double DRIFT_RATE = 0.01;
    private static final double RESYNC_THRESHOLD_SECONDS = 0.25;

    private double offsetSeconds;

    @Getter
    private boolean synchronised;

    /**
     * Feeds the tick of a received packet into the estimate.
     */
    public void onPhysicsUpdate(int physicsTick) {
        double sample = (double) physicsTick / PhysicsPipelineImpl.INSTANCE.getPhysicsSimulationConfig().tickRate() - System.nanoTime() / 1_000_000_000.0;

        if (!synchronised || Math.abs(sample - offsetSeconds) > RESYNC_THRESHOLD_SECONDS) {
            offsetSeconds = sample;
            synchronised = true;
        } else if (sample > offsetSeconds) {
            offsetSeconds = sample;
        } else {
            offsetSeconds += (sample - offsetSeconds) * DRIFT_RATE;
        }
    }

    /**
     * Returns the server physics tick (fractional) that should be on screen right now.
     *
     * @param delayTicks how many ticks to stay behind the newest known server time
     */
    public double renderTick(double delayTicks) {
        return (System.nanoTime() / 1_000_000_000.0 + offsetSeconds) * PhysicsPipelineImpl.INSTANCE.getPhysicsSimulationConfig().tickRate() - delayTicks;
    }
}
