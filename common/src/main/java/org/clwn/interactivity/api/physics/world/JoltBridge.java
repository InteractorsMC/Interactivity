package org.clwn.interactivity.api.physics.world;

import com.github.stephengold.joltjni.PhysicsSystem;
import org.jetbrains.annotations.ApiStatus;

/**
 * A bridge for interacting with the Jolt physics engine.
 */
public interface JoltBridge {
    @ApiStatus.Experimental
    PhysicsSystem getPhysicsSystem();
}
