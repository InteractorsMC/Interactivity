/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.fabric.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import org.clwn.interactivity.core.event.EventBus;
import org.clwn.interactivity.core.event.lifecycle.ServerLifecycleEvents;

public class BusNotifierFabric {
    public static void registerEvents() {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTING.register((server) -> {
            EventBus.publish(new ServerLifecycleEvents.ServerStartingEvent(server));
        });

        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            EventBus.publish(new ServerLifecycleEvents.ServerStoppingEvent(server));
        });

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            registerClientEvents();
        }
    }

    @Environment(EnvType.CLIENT)
    private static void registerClientEvents() {

    }
}
