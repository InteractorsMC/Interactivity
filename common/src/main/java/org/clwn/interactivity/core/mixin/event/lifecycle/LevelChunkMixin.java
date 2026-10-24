/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.mixin.event.lifecycle;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.clwn.interactivity.core.event.lifecycle.BlockChangeEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {
    @Shadow
    @Final
    Level level;

    @Inject(at = @At("RETURN"), method = "setBlockState")
    private void interactivity$onBlockChanged(BlockPos pos, BlockState state, boolean isMoving, CallbackInfoReturnable<BlockState> cir) {
        BlockState oldState = cir.getReturnValue();
        if (oldState != null) {
            BlockChangeEvents.BLOCK_CHANGED_EVENT.publish(new BlockChangeEvents.BlockChangeEvent(level, (LevelChunk) (Object) this, pos, oldState, state));
        }
    }
}
