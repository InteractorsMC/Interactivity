package org.clwn.interactivity.core.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.api.network.NetworkManager;
import org.clwn.interactivity.api.network.NetworkPacket;
import org.clwn.interactivity.api.network.NetworkTransport;
import org.clwn.interactivity.core.event.EventBus;
import org.clwn.interactivity.core.event.lifecycle.PlayerConfigureStateEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class NetworkManagerImpl implements NetworkManager {
    public static NetworkManagerImpl INSTANCE = new NetworkManagerImpl();

    private Map<String, Function<FriendlyByteBuf, ? extends NetworkPacket>> serverListeners = new HashMap<>();
    private Map<String, Function<FriendlyByteBuf, ? extends NetworkPacket>> clientListeners = new HashMap<>();

    private NetworkManagerImpl() {
        EventBus.subscribe(PlayerConfigureStateEvent.class, event -> {
            Player player = event.player();
            configurePlayer(player);
        });
    }

    @Override
    public <T extends NetworkPacket> void registerS2C(
            ResourceLocation identifier,
            Function<FriendlyByteBuf, T> decoder
    ) {
        Interactivity.LOGGER.debug("Registering S2C packet: {}", identifier.toString());
        clientListeners.put(identifier.toString(), decoder);
    }

    @Override
    public <T extends NetworkPacket> void registerC2S(
            ResourceLocation identifier,
            Function<FriendlyByteBuf, T> decoder
    ) {
        Interactivity.LOGGER.debug("Registering C2S packet: {}", identifier.toString());
        serverListeners.put(identifier.toString(), decoder);

    }

    @Override
    public void handle(FriendlyByteBuf buf) {

    }

    @Override
    public void configurePlayer(Player player) {

    }

    @Override
    public NetworkTransport getPlayerTransport(Player player) {
        return null;
    }

    @Override
    public @Nullable NetworkTransport getCurrentTransport() {
        return null;
    }
}
