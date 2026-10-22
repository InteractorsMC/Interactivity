/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core;

import eu.midnightdust.lib.config.MidnightConfig;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.api.client.InteractivityCoreClient;
import org.clwn.interactivity.api.physics.behavior.EventListenerBehavior;
import org.clwn.interactivity.api.physics.behavior.RigidBehavior;
import org.clwn.interactivity.api.physics.behavior.SummonableBehavior;
import org.clwn.interactivity.core.config.InteractivityConfig;
import org.clwn.interactivity.core.event.lifecycle.CommandRegistrationEvents;
import org.clwn.interactivity.core.network.packet.PacketRegistry;
import org.clwn.interactivity.core.physics.PhysicsPipelineImpl;
import org.clwn.interactivity.core.physics.behavior.BehaviorRegistryImpl;
import org.clwn.interactivity.core.physics.command.InteractivityCommand;
import org.clwn.interactivity.core.util.platform.Services;
import org.clwn.interactivity.natives.NativeLibraryLoader;


public class InteractivityCore {
    public static void init() {
        MidnightConfig.init(Interactivity.MOD_ID, InteractivityConfig.class);

        loadNatives();

        PhysicsPipelineImpl.INSTANCE = new PhysicsPipelineImpl();

        registerBehaviors();

        PacketRegistry.registerPackets();

        CommandRegistrationEvents.REGISTER_EVENT.subscribe(context -> InteractivityCommand.register(context.dispatcher()));

        if (Services.PLATFORM.isClientEnvironment()) {
            InteractivityCoreClient.init();
        }
    }

    private static void loadNatives() {
        NativeLibraryLoader.load("joltjni");
        NativeLibraryLoader.load("interactivity_natives");
    }

    private static void registerBehaviors() {
        BehaviorRegistryImpl.INSTANCE.registerBehavior(RigidBehavior.class);
        BehaviorRegistryImpl.INSTANCE.registerBehavior(EventListenerBehavior.class);
        BehaviorRegistryImpl.INSTANCE.registerBehavior(SummonableBehavior.class);
    }
}
