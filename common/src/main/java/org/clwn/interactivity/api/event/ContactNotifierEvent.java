/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.event;

import org.clwn.interactivity.api.physics.event.contact.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Like {@link SubscribeEvent}, but for {@link ContactEvent}.
 * <p>
 * Methods of a body class with {@link org.clwn.interactivity.api.physics.behavior.EventListenerBehavior} receive the
 * contacts of their body. Static methods of a listener class receive the contacts selected by {@link #bodyType1()}.
 */
// todo: rename to ContactListenerEvent and similar for other contact notifier
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ContactNotifierEvent {
    /**
     * Body type id of the body the event is about, only for listener classes. Empty means any body.
     */
    String bodyType1() default "";

    /**
     * Body type id of the touched body. Empty means any body.
     */
    String bodyType2() default "";

    /**
     * Only contacts with terrain.
     */
    boolean terrainOnly() default false;

    /**
     * Minimum speed the bodies move towards each other at the contact point.
     */
    float minApproachSpeed() default 0;
}
