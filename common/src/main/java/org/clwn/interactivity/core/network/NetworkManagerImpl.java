/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.api.network.NetworkManager;
import org.clwn.interactivity.api.network.NetworkPacket;
import org.clwn.interactivity.api.network.NetworkTransport;
import org.clwn.interactivity.api.network.PacketContext;
import org.clwn.interactivity.core.event.lifecycle.PlayerLifecycleEvents;
import org.clwn.interactivity.core.event.lifecycle.ServerLifecycleEvents;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class NetworkManagerImpl implements NetworkManager {
    public static NetworkManagerImpl INSTANCE = new NetworkManagerImpl();

    private final Map<ResourceLocation, Function<FriendlyByteBuf, ? extends NetworkPacket>> serverDecoders = new HashMap<>();
    private final Map<ResourceLocation, Function<FriendlyByteBuf, ? extends NetworkPacket>> clientDecoders = new HashMap<>();

    /**
     * List of all registered network transports
     */
    private final List<NetworkTransport> networkTransports;

    private NetworkManagerImpl() {
        PlayerLifecycleEvents.PLAYER_CONFIGURE_STATE_EVENT.subscribe(event -> {
            Player player = event.player();
            configurePlayer(player);
        });

        networkTransports = ServiceLoader.load(NetworkTransport.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .toList();

        // todo: client should start listening after configuration, but I haven't done this yet and there is no other transports, so it's just startListening()
        // ServerLifecycleEvents.SERVER_STARTING_EVENT.subscribe(serverStartingEvent -> startListening());
        // ServerLifecycleEvents.SERVER_STOPPING_EVENT.subscribe(serverStoppingEvent -> stopListening());
        startListening();
    }

    @Override
    public <T extends NetworkPacket> void registerS2C(
            ResourceLocation identifier,
            Function<FriendlyByteBuf, T> decoder
    ) {
        Interactivity.LOGGER.debug("Registering S2C packet: {}", identifier.toString());
        clientDecoders.put(identifier, decoder);
    }

    @Override
    public <T extends NetworkPacket> void registerC2S(
            ResourceLocation identifier,
            Function<FriendlyByteBuf, T> decoder
    ) {
        Interactivity.LOGGER.debug("Registering C2S packet: {}", identifier.toString());
        serverDecoders.put(identifier, decoder);

    }

    private void startListening() {
        getEnabledTransports().forEach((networkTransport -> networkTransport.startListening(this::handle)));
    }

    private void stopListening() {
        getEnabledTransports().forEach(NetworkTransport::stopListening);
    }

    /**
     * Handles the received network packet.
     * @param buf The buffer containing the packet data.
     */
    private void handle(PacketContext context, ResourceLocation identifier, FriendlyByteBuf buf) {
        Function<FriendlyByteBuf, ? extends NetworkPacket> decoder = null;

        switch (context.receiverSide()) {
            case SERVER -> decoder = serverDecoders.get(identifier);
            case CLIENT -> decoder = clientDecoders.get(identifier);
        }

        if (decoder == null) {
            return;
        }

        NetworkPacket packet = decoder.apply(buf);
        packet.handle(context);
    }

    /**
     * Configures the network transport for the specified player.
     *
     * @param player The player for whom the network transport should be configured.
     */
    private void configurePlayer(Player player) {
        // ...
    }

    @Override
    public NetworkTransport getPlayerTransport(Player player) {
        // todo
        return getAvailableTransports(player).getFirst();
    }

    @Override
    public @Nullable NetworkTransport getCurrentTransport() {
        return getEnabledTransports().getFirst();
    }

    private List<NetworkTransport> getAvailableTransports(Player player) {
        return networkTransports.stream()
                .filter(transport -> transport.canBeUsedForPlayer(player))
                .toList();
    }

    /**
     * Returns sorted list of all enabled transports
     */
    private List<NetworkTransport> getEnabledTransports() {
        return networkTransports.stream()
                .filter(networkTransport -> networkTransport.getPriority() != -1)
                .sorted(Comparator.comparingInt(NetworkTransport::getPriority))
                .toList();
    }
}
