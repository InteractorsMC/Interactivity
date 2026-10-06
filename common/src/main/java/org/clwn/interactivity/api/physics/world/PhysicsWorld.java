/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.world;

import net.minecraft.world.level.Level;

import java.util.concurrent.Executor;

public interface PhysicsWorld extends Executor {
    /**
     * Tick value from what physics world must start simulation (now is minimum signed int value)
     */
    int STARTING_TICK = -2147483648;

    /**
     * Returns current physics tick
     * @return current physics tick
     */
    int getTick();

    boolean isRunning();

    void pauseSimulation();

    void continueSimulation();

    /**
     * Checks if simulation is rolling back
     * @return {@code true} if simulation is rolling back, {@code false} otherwise
     */
    boolean isRollingBack();

    Level getLevel();
}
