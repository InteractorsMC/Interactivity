/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.contact;

import org.clwn.interactivity.api.event.ContactNotifierEvent;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.event.contact.ContactEvent;

import java.util.function.Consumer;

public interface ContactManager {
    /**
     * Queues adding a contact notifier, the runtime version of {@link ContactNotifierEvent}.
     *
     * @param eventType    selects the reported contacts, like the parameter type of {@link ContactNotifierEvent} method
     * @param configurator sets the filters
     * @param callback     receives the contacts after each physics step
     * @return the notifier, for {@link #removeNotifier(ContactNotifier)}
     */
    <E extends ContactEvent> ContactNotifier addNotifier(Class<E> eventType, Consumer<ContactNotifier> configurator, Consumer<E> callback);

    /**
     * Queues the removal of a notifier.
     */
    void removeNotifier(ContactNotifier notifier);

    /**
     * Queues ignoring all collisions between two bodies, even if they are touching already.
     * It ends when either body is removed and is not saved.
     */
    void ignoreCollision(Body body1, Body body2);

    /**
     * Queues the end of {@link #ignoreCollision(Body, Body)}.
     */
    void unignoreCollision(Body body1, Body body2);
}
