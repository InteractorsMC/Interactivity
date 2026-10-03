/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics;

import com.github.stephengold.joltjni.Jolt;
import com.github.stephengold.joltjni.JoltPhysicsObject;
import net.minecraft.world.level.Level;
import org.clwn.interactivity.api.physics.PhysicsPipeline;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;
import org.clwn.interactivity.core.physics.world.PhysicsWorldImpl;
import org.jetbrains.annotations.Nullable;

public class PhysicsPipelineImpl implements PhysicsPipeline {
    public static @Nullable PhysicsPipelineImpl INSTANCE = new PhysicsPipelineImpl();

    static {
        JoltPhysicsObject.startCleaner();
        Jolt.registerDefaultAllocator();

        Jolt.installDefaultTraceCallback();
    }

    private PhysicsPipelineImpl() {

    }

    @Override
    public PhysicsWorld getPhysicsWorld(Level level) {
        return new PhysicsWorldImpl();
    }
}
