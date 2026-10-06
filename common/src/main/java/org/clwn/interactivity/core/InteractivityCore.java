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
import org.clwn.interactivity.api.InteractivityAPI;
import org.clwn.interactivity.api.physics.behavior.EventListenerBehavior;
import org.clwn.interactivity.core.client.render.RenderPipeline;
import org.clwn.interactivity.core.config.InteractivityConfig;
import org.clwn.interactivity.core.physics.PhysicsPipelineImpl;
import org.clwn.interactivity.core.physics.behavior.BehaviorRegistryImpl;
import org.clwn.interactivity.natives.NativeLibraryLoader;


public class InteractivityCore {
    private static final RenderPipeline renderPipeline = new RenderPipeline();

    public static void init() {
        MidnightConfig.init(Interactivity.MOD_ID, InteractivityConfig.class);

        loadNatives();

        PhysicsPipelineImpl.INSTANCE = new PhysicsPipelineImpl();

        registerBehaviors();

        InteractivityAPI.getInstance().getNetworkManager(); // todo: remove. now used to just load class
    }

    private static void loadNatives() {
        NativeLibraryLoader.load("joltjni");
        NativeLibraryLoader.load("interactivity_natives");
    }

    private static void registerBehaviors() {
        BehaviorRegistryImpl.INSTANCE.registerBehavior(EventListenerBehavior.class);
    }
}
