/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.behavior;

import org.clwn.interactivity.api.physics.body.BodySummonConfigurator;
import org.jetbrains.annotations.Nullable;

/**
 * Lets operators summon bodies of this type.
 */
public record SummonableBehavior(@Nullable BodySummonConfigurator configurator) implements Behavior {
    public SummonableBehavior() {
        this(null);
    }
}
