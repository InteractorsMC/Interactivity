/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.world;

import com.github.stephengold.joltjni.enumerate.EPhysicsUpdateError;
import lombok.Getter;
import net.minecraft.world.level.Level;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.api.physics.body.PhysicsStateAccessor;
import org.clwn.interactivity.api.physics.world.JoltBridge;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;
import org.clwn.interactivity.core.config.InteractivityConfig;
import org.clwn.interactivity.core.physics.PhysicsLayerRegistryImpl;
import org.clwn.interactivity.core.physics.body.AbstractBodyManager;
import org.clwn.interactivity.core.physics.system.PhysicsWorldEventDispatcher;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class PhysicsWorldImpl implements PhysicsWorld {
    @Getter
    protected int tick;

    @Getter
    protected boolean isRunning;

    @Getter
    protected boolean rollingBack;

    @Getter
    protected Level level;

    @Getter
    protected float fixedTimeStep;

    protected Thread workingThread;

    protected PhysicsRunner physicsRunner;
    protected PhysicsSimulation physicsSimulation;

    private final Queue<Runnable> executionQueue = new ConcurrentLinkedQueue<>();

    public PhysicsWorldImpl(Level level, PhysicsLayerRegistryImpl physicsLayerRegistry) {
        this.level = level;
        this.fixedTimeStep = 1.0f / InteractivityConfig.physicsTickRate;
        this.physicsRunner = new PhysicsRunner(this::updatePhysics, this::processExecutionQueue, true, this.fixedTimeStep);
        this.physicsSimulation = new PhysicsSimulation(new PhysicsSimulation.Config(
                physicsLayerRegistry,
                65536,         // maxBodies
                65536,         // maxBodyPairs
                65536,         // maxContactConstraints
                10,            // numPositionIterations
                15,            // numVelocityIterations
                0.02f,         // speculativeContactDistance
                0.2f,          // baumgarteFactor
                0.02f,         // penetrationSlop
                1.0f,          // timeBeforeSleep
                0.005f,        // pointVelocitySleepThreshold
                -9.81f,        // gravityY
                64 * 1024 * 1024 // tempAllocatorSize
        ));
    }

    public void init() {
        if (this.workingThread != null && this.workingThread.isAlive()) {
            return;
        }

        this.physicsSimulation.init();

        this.isRunning = true;
        physicsRunner.setPaused(false);
        String threadName = "Interactivity Physics Thread - " + level.dimension().location().getPath();
        this.workingThread = new Thread(physicsRunner, threadName);
        this.workingThread.setDaemon(true);
        this.workingThread.start();
    }

    public void close() {
        if (!this.isRunning) {
            return;
        }
        this.isRunning = false;

        physicsRunner.stop();
        physicsSimulation.close();
    }

    private void updatePhysics() {
        tick++;

        PhysicsWorldEventDispatcher.prePhysicsTick(this);

        int error = this.physicsSimulation.simulate(fixedTimeStep, 1);

        if (error != EPhysicsUpdateError.None) {
            Interactivity.LOGGER.error(
                    "Jolt physics update failed with error code: {}.",
                    error
            );

            this.close();
            return;
        }

        PhysicsWorldEventDispatcher.postPhysicsTick(this);
    }

    private void processExecutionQueue() {
        while (!executionQueue.isEmpty()) {
            Runnable command = this.executionQueue.poll();

            try {
                command.run();
            } catch (Exception e) {
                Interactivity.LOGGER.error("Exception while executing physics command", e);
            }
        }
    }

    @Override
    public void execute(@NotNull Runnable command) {
        this.executionQueue.add(command);
    }

    @Override
    public void pauseSimulation() {

    }

    @Override
    public void continueSimulation() {

    }

    public abstract AbstractBodyManager getBodyManager();

    @Override
    public PhysicsStateAccessor getPhysicsStateAccessor() {
        return getBodyManager().getBodyDataStore();
    }

    @Override
    public JoltBridgeImpl getJoltBridge() {
        return this.physicsSimulation.getJoltBridge();
    }
}
