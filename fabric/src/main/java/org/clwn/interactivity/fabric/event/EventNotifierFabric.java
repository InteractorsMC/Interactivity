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
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.clwn.interactivity.core.event.lifecycle.LevelLifecycleEvents;
import org.clwn.interactivity.core.event.lifecycle.ServerLifecycleEvents;

public class EventNotifierFabric {
    public static void registerEvents() {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTING.register((server) -> ServerLifecycleEvents.SERVER_STARTING_EVENT.publish(new ServerLifecycleEvents.ServerEvent(server)));

        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register((server) -> ServerLifecycleEvents.SERVER_STOPPING_EVENT.publish(new ServerLifecycleEvents.ServerEvent(server)));

        ServerWorldEvents.LOAD.register((server, level) -> LevelLifecycleEvents.LEVEL_LOAD_EVENT.publish(new LevelLifecycleEvents.ServerLevelEvent(level)));

        ServerWorldEvents.UNLOAD.register((server, level) -> LevelLifecycleEvents.LEVEL_UNLOAD_EVENT.publish(new LevelLifecycleEvents.ServerLevelEvent(level)));

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            registerClientEvents();
        }
    }

    @Environment(EnvType.CLIENT)
    private static void registerClientEvents() {

    }
}
