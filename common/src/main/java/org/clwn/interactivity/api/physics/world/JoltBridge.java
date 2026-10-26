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
