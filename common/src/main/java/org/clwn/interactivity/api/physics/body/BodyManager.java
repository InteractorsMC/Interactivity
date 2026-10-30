/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.body;

import com.github.stephengold.joltjni.readonly.QuatArg;
import com.github.stephengold.joltjni.readonly.RVec3Arg;
import com.github.stephengold.joltjni.readonly.Vec3Arg;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;
import java.util.function.Consumer;

public interface BodyManager {
    <T extends Body> T spawnBody(BodyType<T> bodyType, RVec3Arg position, QuatArg rotation, Consumer<T> configurator);

    <T extends Body> void spawnConstructedBody(T body, RVec3Arg position, QuatArg rotation, Vec3Arg linearVelocity, Vec3Arg angularVelocity);

    default void removeBody(Body body, RemovalReason removalReason) {
        removeBody(body.getUuid(), removalReason);
    }
    void removeBody(UUID id, RemovalReason removalReason);

    Body getBody(UUID id);

    void requestDataSync(Body body);

    @ApiStatus.Experimental
    default int getJoltBodyId(Body body) {
        return getJoltBodyId(body.getUuid());
    }

    /**
     * Retrieves Jolt Body id that can be used in {@link org.clwn.interactivity.api.physics.world.JoltBridge}.
     * <p>
     * The existence of this method has not been definitively established, so it may be removed at any time.
     * @param id Body id
     * @return Jolt Body id, if body exists in simulation, {@link com.github.stephengold.joltjni.Jolt#cInvalidBodyId} otherwise
     */
    @ApiStatus.Experimental
    int getJoltBodyId(UUID id);
}
