/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.event;

public interface EventBus {
    /**
     * Register class as event listener.
     * <p>
     * Instead of registering {@link org.clwn.interactivity.api.physics.body.Body} classes, use {@link org.clwn.interactivity.api.physics.behavior.EventListenerBehavior} behavior.
     */
    void registerListener(Class<?> clazz, boolean listensCustomEvents);

    <T extends Event & CustomEvent> void publishEvent(T event);
}
