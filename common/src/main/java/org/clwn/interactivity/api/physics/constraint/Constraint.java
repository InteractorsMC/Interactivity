/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.constraint;

import com.github.stephengold.joltjni.TwoBodyConstraint;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Handle of a constraint between two bodies, or between a body and the world.
 *
 * @param <J> the Jolt constraint class
 */
public interface Constraint<J extends TwoBodyConstraint> {
    UUID getUuid();

    ConstraintType<?, J> getType();

    UUID getBody1();

    /**
     * @return the second body id, or {@code null} if the constraint is attached to the world
     */
    @Nullable UUID getBody2();

    State getState();

    /**
     * Queues an action on the live Jolt constraint and wakes up its bodies.
     */
    void modify(Consumer<J> action);

    enum State {
        /**
         * Created, but not added to the simulation yet.
         */
        PENDING,

        /**
         * Part of the simulation.
         */
        ACTIVE,

        /**
         * Removed, or its creation failed.
         */
        REMOVED
    }
}
