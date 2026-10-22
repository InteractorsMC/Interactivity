package org.clwn.interactivity.fabric.util.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.clwn.interactivity.api.network.PacketContext;
import org.clwn.interactivity.api.util.Side;
import org.clwn.interactivity.core.util.platform.services.INetworkingHelper;

import java.util.function.BiConsumer;

@Environment(EnvType.CLIENT)
public class FabricNetworkingHelperClient {
    protected static <T extends CustomPacketPayload> void registerClientListener(CustomPacketPayload.Type<?> type, BiConsumer<PacketContext, T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(type, ((payload, context) -> handler.accept(new PacketContext(context.player(), Side.CLIENT), (T) payload)));
    }

    protected static void sendPacketToServerInternal(CustomPacketPayload packet, INetworkingHelper.PacketStageType stage) {
        if ((stage == INetworkingHelper.PacketStageType.COMMON && ClientPlayNetworking.canSend(packet.type())) || stage == INetworkingHelper.PacketStageType.PLAY) {
            ClientPlayNetworking.send(packet);
        } else {
            ClientConfigurationNetworking.send(packet);
        }
    }
}
