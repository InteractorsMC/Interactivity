/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.mixin.compat.iris;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import org.clwn.interactivity.core.client.event.render.LevelRenderEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Iris draws entities into its shadow map itself instead of going through {@code LevelRenderer.renderLevel}, so bodies
 * have to be submitted separately. This happens at the "draw entities" profiler section, before Iris readies up its
 * buffers: anything written after that is never drawn and gets discarded with a warning.
 * <p>
 * Skipped when Iris is not installed, and not required, so a changed Iris version only loses the shadows.
 */
@Pseudo
@Mixin(targets = "net.irisshaders.iris.shadows.ShadowRenderer", remap = false)
public class ShadowRendererMixin {
    @Inject(
            method = "renderShadows",
            at = @At(
                    value = "INVOKE_STRING",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
                    args = "ldc=draw entities",
                    remap = true
            ),
            require = 0
    )
    private void interactivity$beforeShadowDraw(
            CallbackInfo ci,
            @Local PoseStack modelView,
            @Local MultiBufferSource.BufferSource bufferSource,
            @Local(argsOnly = true) Camera camera
    ) {
        LevelRenderEvents.ENTITY_PASS.publish(new LevelRenderEvents.EntityPassContext(
                LevelRenderEvents.Pass.SECONDARY, modelView, bufferSource, camera
        ));
    }
}
