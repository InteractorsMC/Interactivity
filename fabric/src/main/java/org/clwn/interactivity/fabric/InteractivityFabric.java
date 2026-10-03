/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.fabric;

import net.fabricmc.api.ModInitializer;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.fabric.event.EventNotifierFabric;

public class InteractivityFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        EventNotifierFabric.registerEvents();

        Interactivity.init();
    }
}
