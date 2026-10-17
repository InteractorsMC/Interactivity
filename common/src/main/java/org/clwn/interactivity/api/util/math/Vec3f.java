/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.util.math;

/**
 * Read-only interface for a vector with single-precision.
 * <p>
 *     It's not expected to be implemented by external code as it is not being used outside public API.
 * </p>
 */
public interface Vec3f {
    float getX();
    float getY();
    float getZ();
}
