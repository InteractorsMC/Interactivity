package org.clwn.interactivity.neoforge.util.platform;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.clwn.interactivity.core.util.platform.services.INetworkingHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@ApiStatus.Internal
public class NeoForgeNetworkingHelper implements INetworkingHelper {
    public record Packet<T extends CustomPacketPayload>(PacketStageType stage, PacketType packetType, CustomPacketPayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec, BiConsumer<Player, T> handler) {}
    public static List<Packet> packetsToRegister = new ArrayList<>();

    @Override
    public <T extends CustomPacketPayload> void registerPacket(PacketStageType stage, PacketType packetType, CustomPacketPayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec, BiConsumer<Player, T> handler) {
        packetsToRegister.add(new Packet(stage, packetType, type, codec, handler));
    }

    @Override
    public void sendPacketToPlayer(Player player, CustomPacketPayload packet, PacketStageType stage, @Nullable ServerConfigurationPacketListenerImpl serverConfigurationPacketListener) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, packet);
        } else {
            throw new IllegalArgumentException("Player must be a ServerPlayer to send packets.");
        }
    }

    @Override
    public void sendPacketToServer(CustomPacketPayload packet, PacketStageType stage) {
        PacketDistributor.sendToServer(packet);
    }
}
