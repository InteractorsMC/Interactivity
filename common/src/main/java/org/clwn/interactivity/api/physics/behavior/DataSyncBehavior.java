/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.behavior;

import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyDataReader;
import org.clwn.interactivity.api.physics.body.BodyDataWriter;

/**
 * Adds state sync with clients.
 * <p>
 * Calling {@link Body#requestDataSync()} is required to re-sync data with clients after changing state.
 */
public record DataSyncBehavior(BodyDataWriter writer, BodyDataReader reader) implements Behavior {
}
