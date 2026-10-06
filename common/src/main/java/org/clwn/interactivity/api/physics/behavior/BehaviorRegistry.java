/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.behavior;

import java.util.Set;

public interface BehaviorRegistry {
    <B extends Behavior> void registerBehavior(Class<B> behaviorClass);

    boolean isRegistered(Class<? extends Behavior> behaviorClass);

    Set<Class<? extends Behavior>> getRegisteredBehaviors();
}
