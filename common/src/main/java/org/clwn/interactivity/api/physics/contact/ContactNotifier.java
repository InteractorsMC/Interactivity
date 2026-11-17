/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.contact;

import lombok.Getter;
import org.clwn.interactivity.api.event.ContactNotifierEvent;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.jetbrains.annotations.Nullable;

/**
 * Selects the contacts of a runtime notifier, like the parameters of {@link ContactNotifierEvent}.
 * Unset filters match anything. The filters are read when the notifier is added, so set them in the configurator.
 */
@Getter
public final class ContactNotifier {
    private @Nullable Body body1;
    private @Nullable BodyType<?> bodyType1;
    private @Nullable Body body2;
    private @Nullable BodyType<?> bodyType2;
    private boolean terrainOnly;
    private float minApproachSpeed;

    /**
     * The body the events are about. The notifier is removed together with it.
     */
    public ContactNotifier body1(Body body) {
        this.body1 = body;
        return this;
    }

    public ContactNotifier bodyType1(BodyType<?> bodyType) {
        this.bodyType1 = bodyType;
        return this;
    }

    /**
     * The touched body. The notifier is removed together with it.
     */
    public ContactNotifier body2(Body other) {
        this.body2 = other;
        return this;
    }

    public ContactNotifier bodyType2(BodyType<?> otherType) {
        this.bodyType2 = otherType;
        return this;
    }

    /**
     * Only contacts with terrain.
     */
    public ContactNotifier terrainOnly() {
        this.terrainOnly = true;
        return this;
    }

    /**
     * Minimum speed the bodies move towards each other at the contact point, ignored for removed contacts.
     */
    public ContactNotifier minApproachSpeed(float minApproachSpeed) {
        this.minApproachSpeed = minApproachSpeed;
        return this;
    }
}
