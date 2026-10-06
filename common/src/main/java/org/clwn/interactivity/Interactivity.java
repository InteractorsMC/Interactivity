/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity;

import org.clwn.interactivity.builtin.InteractivityBuiltin;
import org.clwn.interactivity.core.InteractivityCore;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ApiStatus.Internal
public class Interactivity {
    public static final String MOD_ID = "interactivity";
    public static final String MOD_NAME = "Interactivity";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static void init() {
        InteractivityCore.init();
        InteractivityBuiltin.init();
    }
}