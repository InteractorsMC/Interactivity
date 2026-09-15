package org.clwn.interactivity.fabric.util.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.api.network.PacketContext;
import org.clwn.interactivity.api.util.Side;
import org.clwn.interactivity.core.util.platform.services.INetworkingHelper;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

// todo: blow this shit up and rework fucking everything (related to configuration phase)
public class FabricNetworkingHelper implements INetworkingHelper {

    @Override
    public <T extends CustomPacketPayload> void registerPacket(PacketStageType stage, PacketType packetType, CustomPacketPayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec, BiConsumer<PacketContext, T> handler) {
        Interactivity.LOGGER.debug("Registering packet: {}", type);

        if (stage == PacketStageType.COMMON || stage == PacketStageType.PLAY) {
            if (packetType == PacketType.BIDIRECTIONAL || packetType == PacketType.C2S) {
                PayloadTypeRegistry.playC2S().register(type, codec);
                registerServerListener(type, handler);
            }
            if (packetType == PacketType.BIDIRECTIONAL || packetType == PacketType.S2C) {
                PayloadTypeRegistry.playS2C().register(type, codec);
                registerClientListener(type, handler);
            }
        }
        if (stage == PacketStageType.COMMON || stage == PacketStageType.CONFIGURATION) {
            if (packetType == PacketType.BIDIRECTIONAL || packetType == PacketType.C2S) {
                PayloadTypeRegistry.configurationC2S().register(type, codec);
                registerServerListener(type, handler);
            }
            if (packetType == PacketType.BIDIRECTIONAL || packetType == PacketType.S2C) {
                PayloadTypeRegistry.configurationS2C().register(type, codec);
                registerClientListener(type, handler);
            }
        }
    }


    @Environment(EnvType.CLIENT)
    private static <T extends CustomPacketPayload> void registerClientListener(CustomPacketPayload.Type<?> type, BiConsumer<PacketContext, T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(type, ((payload, context) -> handler.accept(new PacketContext(context.player(), Side.CLIENT), (T) payload)));
    }

    private static <T extends CustomPacketPayload> void registerServerListener(CustomPacketPayload.Type<?> type, BiConsumer<PacketContext, T> handler) {
        ServerPlayNetworking.registerGlobalReceiver(type, ((payload, context) -> handler.accept(new PacketContext(context.player(), Side.SERVER), (T) payload)));
    }

    @Override
    public void sendPacketToPlayer(ServerPlayer player, CustomPacketPayload packet, PacketStageType stage, @Nullable ServerConfigurationPacketListenerImpl serverConfigurationPacketListener) {
        if ((stage == PacketStageType.COMMON && ServerPlayNetworking.canSend(player, packet.type())) || stage == PacketStageType.PLAY) {
            ServerPlayNetworking.send(player, packet);
        } else {
            if (serverConfigurationPacketListener == null) {
                throw new IllegalArgumentException("No configuration packet listener provided");
            }

            ServerConfigurationNetworking.send(serverConfigurationPacketListener, packet);
        }
    }

    @Override
    public void sendPacketToServer(CustomPacketPayload packet, PacketStageType stage) {
        sendPacketToServerInternal(packet, stage);
    }

    @Environment(EnvType.CLIENT)
    private static void sendPacketToServerInternal(CustomPacketPayload packet, PacketStageType stage) {
        if ((stage == PacketStageType.COMMON && ClientPlayNetworking.canSend(packet.type())) || stage == PacketStageType.PLAY) {
            ClientPlayNetworking.send(packet);
        } else {
            ClientConfigurationNetworking.send(packet);
        }
    }
}
