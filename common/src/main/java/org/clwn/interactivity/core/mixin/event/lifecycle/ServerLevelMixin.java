/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.mixin.event.lifecycle;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProgressListener;
import org.clwn.interactivity.core.event.internal.lifecycle.LevelSaveEvents;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    @Inject(at = @At("HEAD"), method = "save")
    private void interactivity$onSave(@Nullable ProgressListener progress, boolean flush, boolean skipSave, CallbackInfo ci) {
        if (!skipSave) {
            LevelSaveEvents.SAVE_EVENT.publish(new LevelSaveEvents.LevelSaveEvent((ServerLevel) (Object) this, flush));
        }
    }
}
