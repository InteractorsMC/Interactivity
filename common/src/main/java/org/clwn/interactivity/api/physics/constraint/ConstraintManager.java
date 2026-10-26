/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.constraint;

import com.github.stephengold.joltjni.TwoBodyConstraint;
import com.github.stephengold.joltjni.TwoBodyConstraintSettings;
import com.github.stephengold.joltjni.enumerate.EConstraintSpace;
import org.clwn.interactivity.api.physics.body.Body;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public interface ConstraintManager {
    /**
     * Queues the creation of a constraint.
     * <p>
     * Points in {@link EConstraintSpace#WorldSpace} are converted with the poses the bodies have when the constraint
     * is created, points in {@link EConstraintSpace#LocalToBodyCom} are relative to the center of mass.
     *
     * @param body1        the first body
     * @param body2        the second body, or {@code null} for attaching to the world
     * @param type         the constraint type
     * @param settings     fills a fresh settings object (points, axes, limits, space)
     * @param configurator sets runtime-only state on the created constraint before it is added
     * @return a {@link Constraint.State#PENDING} handle
     */
    <S extends TwoBodyConstraintSettings, J extends TwoBodyConstraint> Constraint<J> createConstraint(
            Body body1, @Nullable Body body2, ConstraintType<S, J> type,
            Consumer<S> settings, Consumer<J> configurator
    );

    default <S extends TwoBodyConstraintSettings, J extends TwoBodyConstraint> Constraint<J> createConstraint(
            Body body1, @Nullable Body body2, ConstraintType<S, J> type, Consumer<S> settings
    ) {
        return createConstraint(body1, body2, type, settings, constraint -> {});
    }

    /**
     * Queues the removal of a constraint.
     */
    void removeConstraint(Constraint<?> constraint);

    @Nullable Constraint<?> getConstraint(UUID id);

    void forEachConstraint(Body body, Consumer<Constraint<?>> action);
}
