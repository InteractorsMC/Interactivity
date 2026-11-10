/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.body;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Optional;

public interface BodyTypeRegistry {
    Optional<BodyType<? extends Body>> get(ResourceLocation typeId);

    boolean contains(ResourceLocation typeId);

    Collection<BodyType<? extends Body>> getAll();
}
