package org.clwn.interactivity.core;

import eu.midnightdust.lib.config.MidnightConfig;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.api.InteractivityAPI;
import org.clwn.interactivity.api.physics.behavior.EventListenerBehavior;
import org.clwn.interactivity.api.util.Side;
import org.clwn.interactivity.core.client.render.RenderPipeline;
import org.clwn.interactivity.core.config.InteractivityConfig;
import org.clwn.interactivity.core.network.packet.s2c.HelloClientPacket;
import org.clwn.interactivity.core.physics.behavior.BehaviorRegistryImpl;
import org.clwn.interactivity.core.util.platform.Services;
import org.clwn.interactivity.core.util.platform.services.INetworkingHelper;
import org.clwn.interactivity.natives.NativeLibraryLoader;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class InteractivityCore {
    private static final RenderPipeline renderPipeline = new RenderPipeline();

    public static void init() {
        MidnightConfig.init(Interactivity.MOD_ID, InteractivityConfig.class);

        loadNatives();
        registerBehaviors();

        Services.PLATFORM_NETWORK.registerPacket(
                INetworkingHelper.PacketStageType.COMMON,
                INetworkingHelper.PacketType.BIDIRECTIONAL,
                HelloClientPacket.TYPE,
                HelloClientPacket.STREAM_CODEC,
                (context, packet) -> {
                    Interactivity.LOGGER.info("Received HelloClientPacket: {} {}", context.player(), Arrays.toString(packet.data()));
                    if (context.receiverSide() == Side.CLIENT) {
                        Services.PLATFORM_NETWORK.sendPacketToServer(new HelloClientPacket("Hello from client!".getBytes(StandardCharsets.UTF_8)), INetworkingHelper.PacketStageType.COMMON);
                    }
                }
        );

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
