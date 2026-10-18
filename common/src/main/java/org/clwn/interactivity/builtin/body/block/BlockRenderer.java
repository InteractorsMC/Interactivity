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

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.clwn.interactivity.api.client.physics.render.BodyRenderer;
import org.clwn.interactivity.api.client.render.ExtractionContext;
import org.clwn.interactivity.api.client.render.RenderContext;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.util.math.Conversions;

public class BlockRenderer implements BodyRenderer<BlockRenderer.BlockRenderState> {
    @Override
    public BlockRenderState createState() {
        return new BlockRenderState(Vec3.ZERO, Vec3.ZERO);
    }

    @Override
    public void extract(ExtractionContext context, Body body, BlockRenderState state) {
        state.setPosition(Conversions.toMinecraft(body.getPosition()));
        state.setRotation(new Vec3(0, 0, 0));
    }

    @Override
    public void submit(BlockRenderState state, RenderContext context) {
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                context.lowLevel().getPoseStack().last(),
                Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(ItemBlockRenderTypes.getRenderType(Blocks.STONE.defaultBlockState(), false)),
                Blocks.STONE.defaultBlockState(),
                Minecraft.getInstance().getBlockRenderer().getBlockModel(Blocks.STONE.defaultBlockState()),
                255, 255, 255,
                15728880,
                OverlayTexture.NO_OVERLAY
        );
    }

    public static class BlockRenderState {
        @Getter @Setter
        public Vec3 position;
        @Getter @Setter
        public Vec3 rotation;

        public BlockRenderState(Vec3 position, Vec3 rotation) {
            this.position = position;
            this.rotation = rotation;
        }
    }
}
