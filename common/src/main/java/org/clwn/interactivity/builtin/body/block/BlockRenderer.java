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
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.clwn.interactivity.api.client.physics.render.BodyRenderer;
import org.clwn.interactivity.api.client.render.ExtractionContext;
import org.clwn.interactivity.api.client.render.RenderContext;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.util.math.Conversions;

public class BlockRenderer implements BodyRenderer<BlockRenderer.BlockRenderState> {
    private static final BlockState blockState = Blocks.STONE.defaultBlockState();

    @Override
    public BlockRenderState createState() {
        return new BlockRenderState();
    }

    @Override
    public void extract(ExtractionContext context, Body body, BlockRenderState state) {
        int color = Minecraft.getInstance().getBlockColors().getColor(blockState, body.getPhysicsWorld().getLevel(), BlockPos.containing(Conversions.toMinecraft(body.getPosition())), 0);
        state.r = (color >> 16 & 255) / 255.0f;
        state.g = (color >> 8 & 255) / 255.0f;
        state.b = (color & 255) / 255.0f;
        state.packedLight = context.getPackedLight();
    }

    @Override
    public void submit(BlockRenderState state, RenderContext context) {
        context.lowLevel().getPoseStack().translate(-0.5, -0.5, -0.5);

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                context.lowLevel().getPoseStack().last(),
                Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(ItemBlockRenderTypes.getRenderType(blockState, false)),
                blockState,
                Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState),
                state.r, state.g, state.b,
                state.packedLight,
                OverlayTexture.NO_OVERLAY
        );
    }

    public static class BlockRenderState {
        public float r;
        public float g;
        public float b;
        public int packedLight;

        public BlockRenderState() {}
    }
}
