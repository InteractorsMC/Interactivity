/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.world;

import com.github.stephengold.joltjni.*;
import com.github.stephengold.joltjni.readonly.ConstBodyLockInterface;
import com.github.stephengold.joltjni.readonly.ConstBodyLockInterfaceNoLock;
import com.github.stephengold.joltjni.readonly.ConstBroadPhaseQuery;
import com.github.stephengold.joltjni.readonly.ConstNarrowPhaseQuery;
import org.jetbrains.annotations.ApiStatus;

/**
 * A bridge for interacting with the Jolt physics engine.
 */
public interface JoltBridge {
    @ApiStatus.Experimental
    PhysicsSystem getPhysicsSystem();

    BodyInterface getBodyInterface();
    BodyInterface getBodyInterfaceNoLock();

    ConstBodyLockInterface getBodyLockInterface();
    ConstBodyLockInterfaceNoLock getBodyLockInterfaceNoLock();

    ConstBroadPhaseQuery getBroadPhaseQuery();
    ConstNarrowPhaseQuery getNarrowPhaseQuery();
}
