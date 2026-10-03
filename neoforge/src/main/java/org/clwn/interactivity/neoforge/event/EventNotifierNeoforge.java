/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.neoforge.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.core.event.Event;
import org.clwn.interactivity.core.event.lifecycle.ServerLifecycleEvents;

@EventBusSubscriber(modid = Interactivity.MOD_ID)
public class EventNotifierNeoforge {
    @SubscribeEvent
    public static void serverStart(ServerStartingEvent event) {
        ServerLifecycleEvents.SERVER_STARTING_EVENT.publish(new ServerLifecycleEvents.ServerEvent(event.getServer()));
    }

    @SubscribeEvent
    public static void serverStop(ServerStoppingEvent event) {
        ServerLifecycleEvents.SERVER_STOPPING_EVENT.publish(new ServerLifecycleEvents.ServerEvent(event.getServer()));
    }
}
