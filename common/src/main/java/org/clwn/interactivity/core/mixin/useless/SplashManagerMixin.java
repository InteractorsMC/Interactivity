/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.mixin.useless;

import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import org.clwn.interactivity.core.config.InteractivityConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;


@Mixin(SplashManager.class)
public class SplashManagerMixin {
    @Shadow
    @Final
    private static RandomSource RANDOM;

    @Shadow
    @Final
    private List<String> splashes;


    @Unique
    private static final List<String> INTERACTIVITY_SPLASHES = List.of(
            "Surprisingly, thrown stone shatters glass!"
    );

    @Inject(
            method = "apply",
            at = @At("TAIL")
    )
    private void onApply(
            List<String> object,
            ResourceManager resourceManager,
            ProfilerFiller profiler,
            CallbackInfo ci
    ) {
       splashes.addAll(INTERACTIVITY_SPLASHES);
    }

    @Inject(
            method = "getSplash",
            at = @At("HEAD"),
            cancellable = true
    )
    private void getSplash(CallbackInfoReturnable<SplashRenderer> cir) {
        if (InteractivityConfig.uselessDevOption) {
            cir.setReturnValue(new SplashRenderer(INTERACTIVITY_SPLASHES.get(RANDOM.nextInt(INTERACTIVITY_SPLASHES.size()))));
        }
    }
}
