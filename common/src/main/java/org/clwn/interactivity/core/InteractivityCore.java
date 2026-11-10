/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core;

import eu.midnightdust.lib.config.MidnightConfig;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.api.client.InteractivityCoreClient;
import org.clwn.interactivity.api.physics.behavior.*;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.clwn.interactivity.api.physics.body.RegisterBodyTypesEvent;
import org.clwn.interactivity.core.client.physics.ClientPhysicsPipelineImpl;
import org.clwn.interactivity.core.config.InteractivityConfig;
import org.clwn.interactivity.core.event.external.EventBusImpl;
import org.clwn.interactivity.core.event.internal.lifecycle.CommandRegistrationEvents;
import org.clwn.interactivity.core.event.internal.lifecycle.ModLoaderLifecycleEvents;
import org.clwn.interactivity.core.network.packet.PacketRegistry;
import org.clwn.interactivity.core.physics.PhysicsPipelineImpl;
import org.clwn.interactivity.core.physics.behavior.BehaviorRegistryImpl;
import org.clwn.interactivity.core.physics.body.type.BodyTypeRegistryImpl;
import org.clwn.interactivity.core.physics.command.InteractivityCommand;
import org.clwn.interactivity.core.util.platform.Services;
import org.clwn.interactivity.natives.NativeLibraryLoader;

import java.util.function.Consumer;


public class InteractivityCore {
    @Getter
    private static boolean registrationPhaseDone;

    public static void init() {
        MidnightConfig.init(Interactivity.MOD_ID, InteractivityConfig.class);

        loadNatives();

        if (Services.PLATFORM.isClientEnvironment()) {
            ClientPhysicsPipelineImpl.INSTANCE = new ClientPhysicsPipelineImpl();
            PhysicsPipelineImpl.INSTANCE = ClientPhysicsPipelineImpl.INSTANCE;
        } else {
            PhysicsPipelineImpl.INSTANCE = new PhysicsPipelineImpl();
        }

        registerBehaviors();

        PacketRegistry.registerPackets();

        CommandRegistrationEvents.REGISTER_EVENT.subscribe(context -> InteractivityCommand.register(context.dispatcher()));
        ModLoaderLifecycleEvents.LOAD_COMPLETE_EVENT.subscribe(event -> registerContent());

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
        BehaviorRegistryImpl.INSTANCE.registerBehavior(PersistenceBehavior.class);
        BehaviorRegistryImpl.INSTANCE.registerBehavior(DataSyncBehavior.class);
    }

    public static void registerContent() {
        if (registrationPhaseDone) {
            return;
        }
        registrationPhaseDone = true;

        EventBusImpl.INSTANCE.publishEvent(new RegisterBehaviorsEvent() {
            @Override
            public <B extends Behavior> void register(Class<B> behaviorClass) {
                BehaviorRegistryImpl.INSTANCE.registerBehavior(behaviorClass);
            }
        });
        BehaviorRegistryImpl.INSTANCE.freeze();

        EventBusImpl.INSTANCE.publishEvent(new RegisterBodyTypesEvent() {
            @Override
            public <T extends Body> BodyType<T> register(
                    ResourceLocation typeId,
                    Class<T> bodyClass,
                    BodyType.Factory<T> factory,
                    Consumer<BodyType.Builder<T>> configure
            ) {
                return BodyTypeRegistryImpl.INSTANCE.registerBodyType(typeId, bodyClass, factory, configure);
            }
        });
        BodyTypeRegistryImpl.INSTANCE.freeze();

        if (Services.PLATFORM.isClientEnvironment()) {
            InteractivityCoreClient.registerContent();
        }

        Interactivity.LOGGER.info(
                "Registered {} behaviors and {} body types",
                BehaviorRegistryImpl.INSTANCE.getRegisteredBehaviors().size(),
                BodyTypeRegistryImpl.INSTANCE.getAll().size()
        );
    }
}
