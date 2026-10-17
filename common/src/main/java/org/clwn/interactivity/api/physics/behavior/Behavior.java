/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.behavior;

/**
 * Immutable capability/configuration attached to a BodyType.
 * <p>
 *     Behavior does not execute runtime logic. Core systems interpret it.
 * </p>
 */
// todo: possible make behaviors parametrized so behaviors like RigidBehavior (and RigidProvider) can pass body type instead of abstract one
public interface Behavior {
}
