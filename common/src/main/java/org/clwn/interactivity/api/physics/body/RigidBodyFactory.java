/*
 * Copyright (c) 2025-2026 xI-Mx-Ix
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.body;

import com.github.stephengold.joltjni.BodyCreationSettings;
import com.github.stephengold.joltjni.Jolt;
import com.github.stephengold.joltjni.ShapeSettings;

@FunctionalInterface
public interface RigidBodyFactory {
    /**
     * Creates a Jolt body from the given collision shape and adds it to the physics world.
     * The implementation of this method (within VxServerBodyManager) is responsible for
     * the proper resource management of the Jolt objects.
     *
     * @param shape        The collision shape wrapper describing the geometry.
     * @param bodySettings The settings for the physical properties.
     * @return The Jolt body ID of the created body, or {@link Jolt#cInvalidBodyId} on failure.
     */
    int create(ShapeSettings shape, BodyCreationSettings bodySettings);
    // todo: maybe use custom shapes like in Velthoric, but idk if this even make sense
}