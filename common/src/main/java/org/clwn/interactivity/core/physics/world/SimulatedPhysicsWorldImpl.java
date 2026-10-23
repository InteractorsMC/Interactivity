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
import org.clwn.interactivity.api.physics.world.SimulatedPhysicsWorld;
import org.clwn.interactivity.core.config.InteractivityConfig;
import org.clwn.interactivity.core.physics.body.SimulatedBodyManager;
import org.clwn.interactivity.core.physics.system.SimulationEventDispatcher;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class SimulatedPhysicsWorldImpl extends PhysicsWorldImpl implements SimulatedPhysicsWorld {
    @Getter
    protected final float fixedTimeStep;

    @Getter
    protected boolean rollingBack;

    protected Thread workingThread;

    protected final PhysicsRunner physicsRunner;
    protected final PhysicsSimulation physicsSimulation;

    @Getter
    protected BatchPhysicsSync batchPhysicsSync;

    private final Queue<Runnable> executionQueue = new ConcurrentLinkedQueue<>();

    public SimulatedPhysicsWorldImpl(Level level, PhysicsSimulationConfig physicsSimulationConfig) {
        super(level);
        this.fixedTimeStep = 1.0f / InteractivityConfig.physicsTickRate;
        this.physicsRunner = new PhysicsRunner(this::updatePhysics, this::processExecutionQueue, true, this.fixedTimeStep);
        this.physicsSimulation = new PhysicsSimulation(physicsSimulationConfig);

    }

    @Override
    public abstract SimulatedBodyManager getBodyManager();

    @Override
    public void init() {
        if (this.workingThread != null && this.workingThread.isAlive()) {
            return;
        }

        this.physicsSimulation.init();
        this.batchPhysicsSync = new BatchPhysicsSync(
                this.physicsSimulation.getJoltBridge().getPhysicsSystem().va(),
                getBodyManager().getBodyDataStore()
        );

        this.isRunning = true;
        physicsRunner.setPaused(false);
        String threadName = "Interactivity Physics Thread - " + level.dimension().location().getPath();
        this.workingThread = new Thread(physicsRunner, threadName);
        this.workingThread.setDaemon(true);
        this.workingThread.start();
    }

    @Override
    public void close() {
        if (!this.isRunning) {
            return;
        }
        this.isRunning = false;

        physicsRunner.stop();

        // The simulation must not be freed while the physics thread may still be inside Jolt.
        // close() can also be called by the physics thread itself (failed update), in that case it can't wait for itself.
        if (this.workingThread != null && this.workingThread != Thread.currentThread()) {
            try {
                this.workingThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        physicsSimulation.close();
    }

    private void updatePhysics() {
        // noinspection NonAtomicOperationOnVolatileField
        tick++;

        SimulationEventDispatcher.prePhysicsTick(this, fixedTimeStep);

        int error = this.physicsSimulation.simulate(fixedTimeStep, 1);

        if (error != EPhysicsUpdateError.None) {
            Interactivity.LOGGER.error(
                    "Jolt physics update failed with error code: {}.",
                    error
            );

            this.close();
            return;
        }

        this.batchPhysicsSync.sync();

        SimulationEventDispatcher.postPhysicsTick(this, fixedTimeStep);

        try {
            getBodyManager().onPhysicsTick();
            afterSync(tick);
        } catch (Exception e) {
            Interactivity.LOGGER.error("Exception after physics sync", e);
        } finally {
            this.batchPhysicsSync.clear();
        }
    }

    /**
     * Called on the physics thread after the Jolt state of this tick was copied into the data store.
     * The dirty markers of {@link #batchPhysicsSync} are valid during this call and cleared right after it.
     */
    protected void afterSync(int tick) {
    }

    private void processExecutionQueue() {
        Runnable command;
        while ((command = this.executionQueue.poll()) != null) {
            try {
                command.run();
            } catch (Exception e) {
                Interactivity.LOGGER.error("Exception while executing physics command", e);
            }
        }
    }

    /**
     * Queues a command to run on the physics thread before the next simulation step.
     * <p>
     * It is never run inline, even when called from the physics thread, because the caller may be inside a Jolt callback.
     */
    @Override
    public void execute(@NotNull Runnable command) {
        this.executionQueue.add(command);
    }

    @Override
    public void pauseSimulation() {
        // todo
    }

    @Override
    public void continueSimulation() {
        // todo
    }

    @Override
    public JoltBridgeImpl getJoltBridge() {
        return this.physicsSimulation.getJoltBridge();
    }
}
