/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity;

import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.clwn.interactivity.core.config.InteractivityConfig;
import org.clwn.interactivity.core.network.packet.s2c.HelloClientPacket;
import org.clwn.interactivity.core.util.platform.Services;
import org.clwn.interactivity.core.util.platform.services.INetworkingHelper;
import org.clwn.interactivity.natives.NativeLibraryLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

// This class is part of the common project meaning it is shared between all supported loaders. Code written here can only
// import and access the vanilla codebase, libraries used by vanilla, and optionally third party libraries that provide
// common compatible binaries. This means common code can not directly use loader specific concepts such as Forge events
// however it will be compatible with all supported mod loaders.
public class Interactivity {
    public static final String MOD_ID = "interactivity";
    public static final String MOD_NAME = "Interactivity";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static void init() {
        MidnightConfig.init(MOD_ID, InteractivityConfig.class);

        loadNatives();

        Services.PLATFORM_NETWORK.registerPacket(
                INetworkingHelper.PacketStageType.COMMON,
                INetworkingHelper.PacketType.BIDIRECTIONAL,
                HelloClientPacket.TYPE,
                HelloClientPacket.STREAM_CODEC,
                (player, packet) -> {
                    LOGGER.info("Received HelloClientPacket: {} {}", player, Arrays.toString(packet.data()));
                    if (!(player instanceof ServerPlayer)) {
                        Services.PLATFORM_NETWORK.sendPacketToServer(new HelloClientPacket("Hello from client!".getBytes(StandardCharsets.UTF_8)), INetworkingHelper.PacketStageType.COMMON);
                    }
                }
                );
    }

    private static void loadNatives() {
        NativeLibraryLoader.load("joltjni");
        NativeLibraryLoader.load("interactivity_natives");
    }

    public static ResourceLocation getIdentifier(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static Component getTranslation(String key, Object... args) {
        return Component.translatable(key, args);
    }
}