package org.clwn.interactivity.core.client.render;

import com.github.stephengold.joltjni.Quat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.clwn.interactivity.core.event.render.LevelExtractionEvents;
import org.clwn.interactivity.core.event.render.LevelRenderEvents;

public class RenderPipeline {
    public RenderPipeline() {
        LevelExtractionEvents.EXTRACTION_END.subscribe(this::extractRenderData);
        LevelRenderEvents.AFTER_ENTITIES.subscribe(this::renderBodies);
    }

    public void extractRenderData(LevelExtractionEvents.LevelExtractionContext context) {

    }

    public void renderBodies(LevelRenderEvents.LevelRenderContext context) {
        PoseStack poseStack = new PoseStack();

        BlockState blockState = Blocks.LIME_CONCRETE.defaultBlockState();

        poseStack.pushPose();

        poseStack.translate(0 - context.camera().getPosition().x(), 0 - context.camera().getPosition().y(), 0 - context.camera().getPosition().z());

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                blockState,
                poseStack,
                Minecraft.getInstance().renderBuffers().bufferSource(),
                15728880,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
    }
}
