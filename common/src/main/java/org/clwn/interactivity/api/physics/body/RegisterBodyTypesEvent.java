/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.body;

import net.minecraft.resources.ResourceLocation;
import org.clwn.interactivity.api.event.RegistrationEvent;

import java.util.function.Consumer;

public abstract class RegisterBodyTypesEvent extends RegistrationEvent {
    public abstract <T extends Body> BodyType<T> register(
            ResourceLocation typeId,
            Class<T> bodyClass,
            BodyType.Factory<T> factory,
            Consumer<BodyType.Builder<T>> configure
    );
}
