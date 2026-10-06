package org.clwn.interactivity.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.jetbrains.annotations.ApiStatus;

/**
 * Provides unstable and game version-dependent API for rendering in the game.
 */
@ApiStatus.Experimental
public interface LowLevelRenderContext {
    GameRenderer getGameRenderer();
    PoseStack getPoseStack();
}
