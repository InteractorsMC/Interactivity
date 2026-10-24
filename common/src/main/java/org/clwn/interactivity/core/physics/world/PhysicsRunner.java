/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.world;

import lombok.Setter;

import java.util.function.BooleanSupplier;

/**
 * A class that runs the physics simulation in a separate thread.
 * <p>
 * Contains:
 * <ul>
 *     <li>Physics ticker - called every time accumulated time reaches the fixed time step</li>
 *     <li>Queue executor - responsible for executing queued tasks</li>
 * </ul>
 */
public class PhysicsRunner implements Runnable {
    @Setter
    private double fixedTimeStep;
    private static final double MAX_ACCUMULATED_TIME = 0.25;
    private static final int MAX_PHYSICS_STEPS_PER_FRAME = 8;

    private volatile boolean isRunning = true;

    private final Runnable physicsUpdater;
    private final Runnable queueExecutor;
    private final BooleanSupplier pauseCondition;

    public PhysicsRunner(Runnable physicsUpdater, Runnable queueExecutor, BooleanSupplier pauseCondition, double fixedTimeStep) {
        this.physicsUpdater = physicsUpdater;
        this.queueExecutor = queueExecutor;
        this.pauseCondition = pauseCondition;
        this.fixedTimeStep = fixedTimeStep;
    }

    @Override
    public void run() {
        long lastTimeNanos = System.nanoTime();
        double accumulator = 0.0;

        while (this.isRunning) {
            this.queueExecutor.run();

            long currentTimeNanos = System.nanoTime();
            double deltaTime = (currentTimeNanos - lastTimeNanos) / 1_000_000_000.0;
            lastTimeNanos = currentTimeNanos;

            // Prevent a long pause/scheduling stall from producing a huge physics update.
            deltaTime = Math.min(deltaTime, MAX_ACCUMULATED_TIME);

            if (!this.pauseCondition.getAsBoolean()) {
                accumulator += deltaTime;
                accumulator = Math.min(accumulator, MAX_ACCUMULATED_TIME);

                int physicsSteps = 0;

                while (accumulator >= fixedTimeStep && physicsSteps < MAX_PHYSICS_STEPS_PER_FRAME && !this.pauseCondition.getAsBoolean()) {
                    physicsUpdater.run();

                    accumulator -= fixedTimeStep;
                    physicsSteps++;
                }
            }

            Thread.yield();
        }

    }

    public void stop() {
        this.isRunning = false;
    }
}
