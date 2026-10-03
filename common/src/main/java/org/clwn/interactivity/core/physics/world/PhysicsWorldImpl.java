/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.world;

import com.github.stephengold.joltjni.*;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;
import org.clwn.interactivity.core.config.InteractivityConfig;

public class PhysicsWorldImpl implements PhysicsWorld {
    public PhysicsWorldImpl() {
        TempAllocator tempAllocator = new TempAllocatorMalloc();

        int numWorkerThreads = Math.min(InteractivityConfig.AVAILABLE_CORES, InteractivityConfig.maxCoresAmount);
        JobSystem jobSystem = new JobSystemThreadPool(
                Jolt.cMaxPhysicsJobs, Jolt.cMaxPhysicsBarriers, numWorkerThreads);
    }

    @Override
    public int getTick() {
        return PhysicsWorld.STARTING_TICK;
    }

    @Override
    public void pauseSimulation() {

    }

    @Override
    public void continueSimulation() {

    }
}
