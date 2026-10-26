/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.neoforge;


import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.api.network.PacketContext;
import org.clwn.interactivity.api.util.Side;
import org.clwn.interactivity.builtin.item.physicsgun.PhysicsGunItem;
import org.clwn.interactivity.core.physics.command.argument.BodySelectorArgument;
import org.clwn.interactivity.core.util.InteractivityIdentifier;
import org.clwn.interactivity.core.util.platform.services.INetworkingHelper;
import org.clwn.interactivity.neoforge.util.platform.NeoForgeNetworkingHelper;

@Mod(Interactivity.MOD_ID)
public class InteractivityNeoforge {

    public InteractivityNeoforge(IEventBus eventBus) {
        // Use NeoForge to bootstrap the Common mod.
        Interactivity.LOGGER.info("Hello NeoForge world!");
        Interactivity.init();

        eventBus.register(InteractivityNeoforge.class);
    }

    @SubscribeEvent
    public static <T extends CustomPacketPayload> void registerPackets(final RegisterPayloadHandlersEvent event) {
        Interactivity.LOGGER.info("Registering payload handlers for NeoForge.");

        final PayloadRegistrar registrar = event.registrar("1");
        for (NeoForgeNetworkingHelper.Packet<T> packet : NeoForgeNetworkingHelper.packetsToRegister) {
            Interactivity.LOGGER.debug("Registering packet: {}", packet.type().id());
            if (packet.packetType() == INetworkingHelper.PacketType.BIDIRECTIONAL && packet.stage() == INetworkingHelper.PacketStageType.COMMON) {
                registrar.commonBidirectional(
                        packet.type(),
                        packet.codec(),
                        new DirectionalPayloadHandler<>(
                                ((payload, context) -> packet.handler().accept(new PacketContext(context.player(), Side.CLIENT), payload)),
                                ((payload, context) -> packet.handler().accept(new PacketContext(context.player(), Side.SERVER), payload))
                        ));
            } else if (packet.packetType() == INetworkingHelper.PacketType.S2C && packet.stage() == INetworkingHelper.PacketStageType.COMMON) {
                registrar.commonToClient(
                        packet.type(),
                        packet.codec(),
                        (payload, context) -> packet.handler().accept(new PacketContext(context.player(), Side.CLIENT), payload));
            } else if (packet.packetType() == INetworkingHelper.PacketType.C2S && packet.stage() == INetworkingHelper.PacketStageType.COMMON) {
                registrar.commonToServer(
                        packet.type(),
                        packet.codec(),
                        (payload, context) -> packet.handler().accept(new PacketContext(context.player(), Side.SERVER), payload));
            } else if (packet.packetType() == INetworkingHelper.PacketType.BIDIRECTIONAL && packet.stage() == INetworkingHelper.PacketStageType.CONFIGURATION) {
                registrar.configurationBidirectional(
                        packet.type(),
                        packet.codec(),
                        new DirectionalPayloadHandler<>(
                                ((payload, context) -> packet.handler().accept(new PacketContext(context.player(), Side.CLIENT), payload)),
                                ((payload, context) -> packet.handler().accept(new PacketContext(context.player(), Side.SERVER), payload))
                        ));
            } else if (packet.packetType() == INetworkingHelper.PacketType.S2C && packet.stage() == INetworkingHelper.PacketStageType.CONFIGURATION) {
                registrar.configurationToClient(
                        packet.type(),
                        packet.codec(),
                        (payload, context) -> packet.handler().accept(new PacketContext(context.player(), Side.CLIENT), payload));
            } else if (packet.packetType() == INetworkingHelper.PacketType.C2S && packet.stage() == INetworkingHelper.PacketStageType.CONFIGURATION) {
                registrar.configurationToServer(
                        packet.type(),
                        packet.codec(),
                        (payload, context) -> packet.handler().accept(new PacketContext(context.player(), Side.SERVER), payload));
            } else if (packet.packetType() == INetworkingHelper.PacketType.BIDIRECTIONAL && packet.stage() == INetworkingHelper.PacketStageType.PLAY) {
                registrar.playBidirectional(
                        packet.type(),
                        packet.codec(),
                        new DirectionalPayloadHandler<>(
                                ((payload, context) -> packet.handler().accept(new PacketContext(context.player(), Side.CLIENT), payload)),
                                ((payload, context) -> packet.handler().accept(new PacketContext(context.player(), Side.SERVER), payload))
                        ));
            } else if (packet.packetType() == INetworkingHelper.PacketType.S2C && packet.stage() == INetworkingHelper.PacketStageType.PLAY) {
                registrar.playToClient(
                        packet.type(),
                        packet.codec(),
                        (payload, context) -> packet.handler().accept(new PacketContext(context.player(), Side.CLIENT), payload));
            } else if (packet.packetType() == INetworkingHelper.PacketType.C2S && packet.stage() == INetworkingHelper.PacketStageType.PLAY) {
                registrar.playToServer(
                        packet.type(),
                        packet.codec(),
                        (payload, context) -> packet.handler().accept(new PacketContext(context.player(), Side.SERVER), payload));
            }
        }
        NeoForgeNetworkingHelper.packetsToRegister.clear();
    }

    @SubscribeEvent
    public static void registerContent(final RegisterEvent event) {
        event.register(
                Registries.ITEM,
                InteractivityIdentifier.getIdentifier("physics_gun"),
                () -> new PhysicsGunItem(new Item.Properties().stacksTo(1))
        );

        event.register(
                Registries.CREATIVE_MODE_TAB,
                InteractivityIdentifier.getIdentifier("main"),
                () -> CreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.interactivity"))
                        .icon(() -> new ItemStack(BuiltInRegistries.ITEM.get(InteractivityIdentifier.getIdentifier("physics_gun"))))
                        .displayItems((parameters, output) -> output.accept(BuiltInRegistries.ITEM.get(InteractivityIdentifier.getIdentifier("physics_gun"))))
                        .build()
        );

        event.register(
                Registries.COMMAND_ARGUMENT_TYPE,
                InteractivityIdentifier.getIdentifier("body_selector"),
                () -> ArgumentTypeInfos.registerByClass(
                        BodySelectorArgument.class,
                        SingletonArgumentInfo.contextFree(BodySelectorArgument::bodies)
                )
        );

    }
}