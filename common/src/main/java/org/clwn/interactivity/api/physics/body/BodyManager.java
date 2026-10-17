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

import java.util.UUID;
import java.util.function.Consumer;

public interface BodyManager {
    <T extends Body> T spawnBody(BodyType<T> bodyType, RVec3Arg position, QuatArg rotation, Consumer<T> configurator);
    void removeBody(Body body);

    Body getBody(UUID id);
}
