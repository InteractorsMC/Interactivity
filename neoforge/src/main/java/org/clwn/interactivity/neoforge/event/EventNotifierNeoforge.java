/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.neoforge.event;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.core.event.lifecycle.CommandRegistrationEvents;
import org.clwn.interactivity.core.event.lifecycle.LevelLifecycleEvents;
import org.clwn.interactivity.core.event.lifecycle.LevelTickEvents;
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

    @SubscribeEvent
    public static void levelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            LevelTickEvents.END.publish(new LevelTickEvents.ServerLevelTickEvent(serverLevel));
        }
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandRegistrationEvents.REGISTER_EVENT.publish(new CommandRegistrationEvents.CommandRegistrationContext(
                event.getDispatcher(), event.getBuildContext(), event.getCommandSelection()
        ));
    }


    @SubscribeEvent
    public static void levelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            LevelLifecycleEvents.LEVEL_LOAD_EVENT.publish(new LevelLifecycleEvents.ServerLevelEvent(serverLevel));
        } else {
            Interactivity.LOGGER.info("Level load event received for a non-server level: {}", event.getLevel()); // todo: remove
        }
    }

    @SubscribeEvent
    public static void levelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            LevelLifecycleEvents.LEVEL_UNLOAD_EVENT.publish(new LevelLifecycleEvents.ServerLevelEvent(serverLevel));
        } else {
            Interactivity.LOGGER.info("Level unload event received for a non-server level: {}", event.getLevel()); // todo: remove
        }
    }
}
