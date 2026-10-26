/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.constraint;

import com.github.stephengold.joltjni.TwoBodyConstraint;
import com.github.stephengold.joltjni.TwoBodyConstraintRef;
import lombok.Getter;
import org.clwn.interactivity.api.physics.constraint.Constraint;
import org.clwn.interactivity.api.physics.constraint.ConstraintType;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public final class ConstraintImpl<J extends TwoBodyConstraint> implements Constraint<J> {
    private final ConstraintManagerImpl manager;

    @Getter
    private final UUID uuid;

    @Getter
    private final ConstraintType<?, J> type;

    @Getter
    private final UUID body1;

    @Getter
    private final @Nullable UUID body2;

    @Getter
    volatile State state = State.PENDING;

    /**
     * Keeps the Jolt constraint alive while it is {@link State#ACTIVE}.
     */
    @Nullable TwoBodyConstraintRef joltRef;

    @Getter
    @Nullable J joltConstraint;

    ConstraintImpl(ConstraintManagerImpl manager, UUID uuid, ConstraintType<?, J> type, UUID body1, @Nullable UUID body2) {
        this.manager = manager;
        this.uuid = uuid;
        this.type = type;
        this.body1 = body1;
        this.body2 = body2;
    }

    @Override
    public void modify(Consumer<J> action) {
        manager.modify(this, action);
    }
}
