/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.mixin.event.lifecycle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.clwn.interactivity.core.client.event.lifecycle.ClientLevelLifecycleEvents;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow
    @Nullable
    public ClientLevel level;

    @Inject(method = "setLevel", at = @At("HEAD"))
    private void interactivity$beforeSetLevel(ClientLevel newLevel, ReceivingLevelScreen.Reason reason, CallbackInfo ci) {
        if (this.level != null) {
            ClientLevelLifecycleEvents.LEVEL_UNLOAD_EVENT.publish(new ClientLevelLifecycleEvents.ClientLevelEvent(this.level));
        }
    }

    @Inject(method = "setLevel", at = @At("TAIL"))
    private void interactivity$afterSetLevel(ClientLevel newLevel, ReceivingLevelScreen.Reason reason, CallbackInfo ci) {
        ClientLevelLifecycleEvents.LEVEL_LOAD_EVENT.publish(new ClientLevelLifecycleEvents.ClientLevelEvent(newLevel));
    }

    @Inject(method = "clearClientLevel", at = @At("HEAD"))
    private void interactivity$beforeClearLevel(Screen screen, CallbackInfo ci) {
        if (this.level != null) {
            ClientLevelLifecycleEvents.LEVEL_UNLOAD_EVENT.publish(new ClientLevelLifecycleEvents.ClientLevelEvent(this.level));
        }
    }
}
