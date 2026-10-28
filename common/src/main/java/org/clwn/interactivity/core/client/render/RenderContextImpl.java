/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.clwn.interactivity.api.client.render.HighLevelRenderContext;
import org.clwn.interactivity.api.client.render.LowLevelRenderContext;
import org.clwn.interactivity.api.client.render.RenderContext;

final class RenderContextImpl implements RenderContext {
    private final HighLevelRenderContext highLevel = new HighLevelRenderContext() {};
    private final LowLevel lowLevel = new LowLevel();

    void begin(PoseStack poseStack, MultiBufferSource bufferSource) {
        lowLevel.poseStack = poseStack;
        lowLevel.bufferSource = bufferSource;
        lowLevel.gameRenderer = Minecraft.getInstance().gameRenderer;
    }

    @Override
    public HighLevelRenderContext highLevel() {
        return highLevel;
    }

    @Override
    public LowLevelRenderContext lowLevel() {
        return lowLevel;
    }

    private static final class LowLevel implements LowLevelRenderContext {
        private GameRenderer gameRenderer;
        private PoseStack poseStack;
        private MultiBufferSource bufferSource;

        @Override
        public GameRenderer getGameRenderer() {
            return gameRenderer;
        }

        @Override
        public PoseStack getPoseStack() {
            return poseStack;
        }

        @Override
        public MultiBufferSource getBufferSource() {
            return bufferSource;
        }
    }
}
