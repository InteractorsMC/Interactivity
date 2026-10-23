/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.clwn.interactivity.api.client.physics.render.BodyRenderer;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.core.client.event.render.LevelExtractionEvents;
import org.clwn.interactivity.core.client.event.render.LevelRenderEvents;
import org.clwn.interactivity.core.client.physics.ClientPhysicsPipelineImpl;
import org.clwn.interactivity.core.client.physics.render.BodyRendererRegistryImpl;
import org.clwn.interactivity.core.client.physics.world.ClientPhysicsWorldInternal;
import org.clwn.interactivity.core.client.render.sampler.ExtrapolationSampler;
import org.clwn.interactivity.core.client.render.sampler.InterpolationSampler;
import org.clwn.interactivity.core.client.render.sampler.RenderSampler;
import org.clwn.interactivity.core.config.InteractivityConfig;
import org.clwn.interactivity.core.physics.body.store.AbstractBodyDataStore;
import org.clwn.interactivity.core.physics.body.store.PhysicsStateContainer;
import org.jetbrains.annotations.Nullable;

/**
 * Draws all bodies of the client physics world, once per frame, independent of the 20 Hz client tick.
 * <ol>
 *     <li>Extraction (after the frustum was set up). For every body:
 *     create its render state if it has none yet, sample the pose for this frame, cull it,
 *     and let the renderer copy what it needs into its state.</li>
 *     <li>Rendering (after entities). For every visible body: push the pose stack, translate and rotate it to the
 *     sampled pose, let the renderer submit, pop. Renderers therefore always draw in the local space of the body.</li>
 * </ol>
 */
public class RenderDispatcher {
    private static final class RenderSlot {
        Body body;
        BodyRenderer<Object> renderer;
        Object state;
        boolean visible;
        final MutableRenderPose pose = new MutableRenderPose();

        void reset() {
            body = null;
            renderer = null;
            state = null;
            visible = false;
        }
    }

    private RenderSlot[] slots = new RenderSlot[0];
    private int activeSlotCount;

    private final RenderSampler interpolationSampler = new InterpolationSampler();
    private final RenderSampler extrapolationSampler = new ExtrapolationSampler();

    private final ExtractionContextImpl extractionContext = new ExtractionContextImpl();
    private final RenderContextImpl renderContext = new RenderContextImpl();

    public RenderDispatcher() {
        LevelExtractionEvents.EXTRACTION_END.subscribe(this::extract);
        LevelRenderEvents.AFTER_ENTITIES.subscribe(this::render);
    }

    private void extract(LevelExtractionEvents.LevelExtractionContext context) {
        activeSlotCount = 0;

        ClientPhysicsWorldInternal world = ClientPhysicsPipelineImpl.INSTANCE.getCurrentPhysicsWorld();
        if (world == null || !world.getClock().isSynchronised()) {
            return;
        }

        AbstractBodyDataStore store = world.getBodyManager().getBodyDataStore();
        PhysicsStateContainer container = store.getPhysicsStateContainer();
        int slotCount = store.getSlotCount();
        ensureSlots(slotCount);
        activeSlotCount = slotCount;

        double renderTick = world.getClock().renderTick(InteractivityConfig.interpolationDelayTicks);
        RenderSampler sampler = InteractivityConfig.renderSampler == InteractivityConfig.RenderSamplerType.EXTRAPOLATION
                ? extrapolationSampler
                : interpolationSampler;

        Vec3 camera = context.camera().getPosition();
        extractionContext.beginFrame(camera.x, camera.y, camera.z, renderTick);

        for (int i = 0; i < slotCount; i++) {
            RenderSlot slot = slots[i];
            Body body = container.bodies[i];

            slot.visible = false;

            if (body == null) {
                slot.reset();
                continue;
            }

            if (slot.body != body) {
                slot.reset();
                slot.body = body;
                slot.renderer = getRenderer(body);
                slot.state = slot.renderer == null ? null : slot.renderer.createState();
            }

            if (slot.renderer == null) {
                continue;
            }

            sampler.sample(store, i, renderTick, slot.pose);

            if (!isVisible(container, i, slot.pose, context.frustum())) {
                continue;
            }

            extractionContext.setPose(slot.pose);
            slot.renderer.extract(extractionContext, body, slot.state);
            slot.visible = true;
        }
    }

    private void render(LevelRenderEvents.LevelRenderContext context) {
        PoseStack poseStack = new PoseStack();
        renderContext.begin(poseStack, context.gameRenderer());

        Vec3 camera = context.camera().getPosition();

        for (int i = 0; i < activeSlotCount; i++) {
            RenderSlot slot = slots[i];

            if (!slot.visible) {
                continue;
            }

            poseStack.pushPose();
            poseStack.translate(slot.pose.x - camera.x, slot.pose.y - camera.y, slot.pose.z - camera.z);
            poseStack.mulPose(slot.pose.rotation);

            slot.renderer.submit(slot.state, renderContext);

            poseStack.popPose();
        }
    }

    @SuppressWarnings("unchecked")
    private static @Nullable BodyRenderer<Object> getRenderer(Body body) {
        return (BodyRenderer<Object>) BodyRendererRegistryImpl.INSTANCE.getRenderer(body.getType().getTypeId());
    }

    /**
     * Tests the bounding box received from the server, moved to where the body is actually drawn.
     * Rotation between the two states is not accounted for, which the inflation covers.
     */
    private static boolean isVisible(PhysicsStateContainer container, int index, MutableRenderPose pose, Frustum frustum) {
        double shiftX = pose.x - container.posX.get(index);
        double shiftY = pose.y - container.posY.get(index);
        double shiftZ = pose.z - container.posZ.get(index);
        double inflation = InteractivityConfig.cullingInflation;

        return frustum.isVisible(new AABB(
                container.aabbMinX.get(index) + shiftX - inflation,
                container.aabbMinY.get(index) + shiftY - inflation,
                container.aabbMinZ.get(index) + shiftZ - inflation,
                container.aabbMaxX.get(index) + shiftX + inflation,
                container.aabbMaxY.get(index) + shiftY + inflation,
                container.aabbMaxZ.get(index) + shiftZ + inflation
        ));
    }

    private void ensureSlots(int count) {
        if (slots.length >= count) {
            return;
        }

        RenderSlot[] grown = new RenderSlot[Math.max(count, slots.length * 2)];
        System.arraycopy(slots, 0, grown, 0, slots.length);
        for (int i = slots.length; i < grown.length; i++) {
            grown[i] = new RenderSlot();
        }
        slots = grown;
    }
}
