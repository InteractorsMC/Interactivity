/*
 * Copyright (c) 2025-2026 xI-Mx-Ix
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.world;

import com.github.stephengold.joltjni.*;
import lombok.Getter;
import org.clwn.interactivity.core.config.InteractivityConfig;

/**
 * Encapsulates the native Jolt physics engine simulation and its core components.
 * Manages the memory allocators, multi-threading job system, native handlers,
 * and handles the lifecycle (initialization, updates, and cleanup) of the Jolt system.
 */
public class PhysicsSimulation {
    /**
     * The configuration for this physics simulation instance.
     */
    private final PhysicsSimulationConfig config;

    /**
     * The native Jolt Physics System instance.
     */
    private PhysicsSystem physicsSystem;

    /**
     * The multithreaded job system for parallelizing physics calculations.
     */
    private JobSystemThreadPool jobSystem;

    /**
     * Allocator for temporary memory used during the physics update.
     */
    private TempAllocator tempAllocator;

    @Getter
    private JoltBridgeImpl joltBridge;

    /**
     * Constructs a new physics simulation instance with the specified configuration.
     *
     * @param config The physics configuration to apply.
     */

    public PhysicsSimulation(PhysicsSimulationConfig config) {
        this.config = config;
    }

    /**
     * Initializes the native Jolt Physics System and all required filters/allocators.
     */
    public void init() {
        this.tempAllocator = new TempAllocatorMalloc();

        int numWorkerThreads = Math.min(InteractivityConfig.AVAILABLE_CORES, InteractivityConfig.maxCoresAmount);
        this.jobSystem = new JobSystemThreadPool(
                Jolt.cMaxPhysicsJobs, Jolt.cMaxPhysicsBarriers, numWorkerThreads);

        this.physicsSystem = new PhysicsSystem();
        BroadPhaseLayerInterface bpli = config.physicsLayerRegistry().getBroadPhaseLayerInterface();
        ObjectVsBroadPhaseLayerFilter ovbpf = config.physicsLayerRegistry().getObjectVsBroadPhaseLayerFilter();
        ObjectLayerPairFilter olpf = config.physicsLayerRegistry().getObjectLayerPairFilter();

        this.physicsSystem.init(this.config.maxBodies(), 0, this.config.maxBodyPairs(), this.config.maxContactConstraints(), bpli, ovbpf, olpf);

        try (PhysicsSettings settings = this.physicsSystem.getPhysicsSettings()) {
            settings.setNumPositionSteps(this.config.numPositionIterations());
            settings.setNumVelocitySteps(this.config.numVelocityIterations());
            settings.setSpeculativeContactDistance(this.config.speculativeContactDistance());
            settings.setBaumgarte(this.config.baumgarteFactor());
            settings.setPenetrationSlop(this.config.penetrationSlop());
            settings.setTimeBeforeSleep(this.config.timeBeforeSleep());
            settings.setPointVelocitySleepThreshold(this.config.pointVelocitySleepThreshold());
            this.physicsSystem.setPhysicsSettings(settings);
        }

        this.physicsSystem.setGravity(0f, this.config.gravityY(), 0f);

        this.physicsSystem.optimizeBroadPhase();

        this.joltBridge = new JoltBridgeImpl(this.physicsSystem);
    }

    /**
     * Steps the simulation forward by the specified fixed delta time.
     *
     * @param deltaTime      The time step to advance (in seconds).
     * @param collisionSteps Number of collision steps to perform.
     * @return The error code generated during the update (0 means success).
     */
    public int simulate(float deltaTime, int collisionSteps) {
        if (this.physicsSystem == null || this.tempAllocator == null || this.jobSystem == null) {
            return -1; // General failure if not initialized
        }
        return this.physicsSystem.update(deltaTime, collisionSteps, this.tempAllocator, this.jobSystem);
    }

    public void close() {
        this.jobSystem.close();
        this.tempAllocator.close();
    }
}
