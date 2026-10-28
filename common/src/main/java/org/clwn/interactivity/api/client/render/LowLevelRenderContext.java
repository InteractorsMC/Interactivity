package org.clwn.interactivity.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.ApiStatus;

/**
 * Provides unstable and game version-dependent API for rendering in the game.
 */
@ApiStatus.Experimental
public interface LowLevelRenderContext {
    GameRenderer getGameRenderer();
    PoseStack getPoseStack();

    /**
     * The buffers of the current pass.
     * <p>
     * This is not always the main buffer source, so renderers must not fetch the buffers from {@code Minecraft} themselves.
     */
    MultiBufferSource getBufferSource();
}
