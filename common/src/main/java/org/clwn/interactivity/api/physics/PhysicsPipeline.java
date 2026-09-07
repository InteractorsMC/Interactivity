/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics;

import net.minecraft.world.level.Level;
import org.clwn.interactivity.core.physics.PhysicsPipelineImpl;
import org.jetbrains.annotations.Nullable;

/**
 * Controls all simulation configuration across all {@link PhysicsWorld} instances.
 */
public interface PhysicsPipeline {
    @Nullable static PhysicsPipeline getInstance() {
        return PhysicsPipelineImpl.INSTANCE;
    }

    PhysicsWorld getPhysicsWorld(Level level);
}
