/*
 * This file is dedicated to the public domain worldwide using the
 * Creative Commons CC0 1.0 Universal waiver.
 *
 * To the extent possible under law, the author(s) have waived all
 * copyright and related or neighboring rights to this file.
 * You can copy, modify, distribute, and perform the work, even for
 * commercial purposes, all without asking permission.
 *
 * For full legal text, see: https://creativecommons.org
 */
package org.clwn.interactivity.builtin.body.block;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Blocks;
import org.clwn.interactivity.api.client.physics.render.BodyRenderer;
import org.clwn.interactivity.api.client.render.ExtractionContext;
import org.clwn.interactivity.api.client.render.RenderContext;
import org.clwn.interactivity.api.physics.body.Body;

public class BlockRenderer implements BodyRenderer<BlockRenderer.BlockRenderState> {
    @Override
    public BlockRenderState createState() {
        return new BlockRenderState(0);
    }

    @Override
    public void extract(ExtractionContext context, Body body, BlockRenderState state) {
        state.packedLight = context.getPackedLight();
    }

    @Override
    public void submit(BlockRenderState state, RenderContext context) {
        context.lowLevel().getPoseStack().translate(-0.5, -0.5, -0.5);

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                context.lowLevel().getPoseStack().last(),
                Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(ItemBlockRenderTypes.getRenderType(Blocks.STONE.defaultBlockState(), false)),
                Blocks.STONE.defaultBlockState(),
                Minecraft.getInstance().getBlockRenderer().getBlockModel(Blocks.STONE.defaultBlockState()),
                255, 255, 255,
                state.packedLight,
                OverlayTexture.NO_OVERLAY
        );
    }

    public static class BlockRenderState {
        public int packedLight;

        public BlockRenderState(int packedLight) {
            this.packedLight = packedLight;
        }
    }
}
