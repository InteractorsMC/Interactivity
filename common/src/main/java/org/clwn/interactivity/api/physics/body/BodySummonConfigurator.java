/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.body;

import net.minecraft.nbt.CompoundTag;

/**
 * Applies the data given to a summon command.
 * <p>
 * Does not require requesting data sync.
 */
@FunctionalInterface
public interface BodySummonConfigurator {
    void configure(Body body, CompoundTag data);
}
