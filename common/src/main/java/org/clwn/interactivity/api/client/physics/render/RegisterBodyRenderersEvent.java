/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.client.physics.render;

import org.clwn.interactivity.api.event.RegistrationEvent;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyType;

public abstract class RegisterBodyRenderersEvent extends RegistrationEvent {
    public abstract <B extends Body> void register(BodyType<B> bodyType, BodyRenderer<?> renderer);
}
