/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.builtin.item.physicsgun.PhysicsGunItem;
import org.clwn.interactivity.core.util.InteractivityIdentifier;
import org.clwn.interactivity.fabric.event.EventNotifierFabric;

public class InteractivityFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        EventNotifierFabric.registerEvents();

        Interactivity.init();

        Item physicsGun = Registry.register(
                BuiltInRegistries.ITEM,
                InteractivityIdentifier.getIdentifier("physics_gun"),
                new PhysicsGunItem(new Item.Properties().stacksTo(1))
        );

        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                InteractivityIdentifier.getIdentifier("main"),
                FabricItemGroup.builder()
                        .title(Component.translatable("itemGroup.interactivity"))
                        .icon(() -> new ItemStack(physicsGun))
                        .displayItems((parameters, output) -> output.accept(physicsGun))
                        .build()
        );
    }
}
