package org.clwn.interactivity.core.mixin.debug;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.clwn.interactivity.core.client.debug.PhysicsDebugOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(at = @At("RETURN"), method = "getGameInformation")
    private void interactivity$addPhysicsInfo(CallbackInfoReturnable<List<String>> cir) {
        PhysicsDebugOverlay.addLines(minecraft, cir.getReturnValue());
    }
}
