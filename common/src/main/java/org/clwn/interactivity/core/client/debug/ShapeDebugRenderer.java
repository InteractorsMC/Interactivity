/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.client.debug;

import com.github.stephengold.joltjni.BodyLockRead;
import com.github.stephengold.joltjni.Jolt;
import com.github.stephengold.joltjni.readonly.ConstShape;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerLevel;
import org.clwn.interactivity.api.client.render.ExtractionContext;
import org.clwn.interactivity.api.client.render.RenderContext;
import org.clwn.interactivity.api.client.render.RenderPose;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.util.math.Conversions;
import org.clwn.interactivity.api.util.math.Vec3d;
import org.clwn.interactivity.core.physics.PhysicsPipelineImpl;
import org.clwn.interactivity.core.physics.body.store.SimulatedPhysicsStateContainer;
import org.clwn.interactivity.core.physics.world.SimulatedPhysicsWorldImpl;
import org.jetbrains.annotations.Nullable;

import java.nio.FloatBuffer;
import java.util.*;

// todo: idk, maybe optimize this? I'm sleepy af rn so idc
public class ShapeDebugRenderer {
    private static final float[] NO_SHAPE = new float[0];

    public static ShapeDebugRenderer INSTANCE = new ShapeDebugRenderer();

    private final Map<Body, float[]> shapes = new WeakHashMap<>();

    private List<DebugRenderState> debugRenderStateList = new ArrayList<>(512);

    private record DebugRenderState(
            double AABBMinX,
            double AABBMinY,
            double AABBMinZ,
            double AABBMaxX,
            double AABBMaxY,
            double AABBMaxZ,
            boolean isActive,
            float[] shape,
            RenderPose renderPose,
            Vec3d camera
            ) {

    }

    private @Nullable SimulatedPhysicsWorldImpl physicsWorld;

    private ShapeDebugRenderer() {

    }

    public boolean isEnabled() {
        return Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes();
    }

    public void newFrame() {
        debugRenderStateList.clear();

        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) {
            return;
        }
        ServerLevel serverLevel = server.getLevel(Minecraft.getInstance().level.dimension());
        if (serverLevel == null || !(PhysicsPipelineImpl.INSTANCE.getPhysicsWorld(serverLevel) instanceof SimulatedPhysicsWorldImpl world)) {
            return;
        }

        this.physicsWorld = world;
    }

    public void extract(ExtractionContext context, Body body) {
        int index = physicsWorld.getBodyManager().getBodyDataStore().getIndexByUuid(body.getUuid());
        SimulatedPhysicsStateContainer container = physicsWorld.getBodyManager().getBodyDataStore().getPhysicsStateContainer();

        debugRenderStateList.add(new DebugRenderState(
                container.aabbMinX.get(index) - context.getCameraPosition().getX(),
                container.aabbMinY.get(index) - context.getCameraPosition().getY(),
                container.aabbMinZ.get(index) - context.getCameraPosition().getZ(),
                container.aabbMaxX.get(index) - context.getCameraPosition().getX(),
                container.aabbMaxY.get(index) - context.getCameraPosition().getY(),
                container.aabbMaxZ.get(index) - context.getCameraPosition().getZ(),
                container.isActive.get(index) != 0,
                getShape(body, container.joltBodyId.get(index)),
                context.getPose(),
                context.getCameraPosition()
                ));
    }

    private float[] getShape(Body body, int joltBodyId) {
        float[] shape = shapes.get(body);
        if (shape != null) {
            return shape;
        }

        // Marks the request as sent until the physics thread answers
        shapes.put(body, NO_SHAPE);

        physicsWorld.execute(() -> {
            try (BodyLockRead lock = new BodyLockRead(physicsWorld.getJoltBridge().getBodyLockInterfaceNoLock(), joltBodyId)) {
                shapes.put(body, lock.succeeded() ? readTriangles(lock.getBody().getShape()) : NO_SHAPE);
            }
        });
        return NO_SHAPE;
    }

    private static float[] readTriangles(ConstShape shape) {
        int triangleCount = shape.countDebugTriangles();
        if (triangleCount == 0) {
            return NO_SHAPE;
        }

        FloatBuffer buffer = Jolt.newDirectFloatBuffer(triangleCount * 9);
        shape.copyDebugTriangles(buffer);

        float[] triangles = new float[triangleCount * 9];
        buffer.get(0, triangles);
        return triangles;
    }

    public void submit(RenderContext context) {
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());

        PoseStack poseStack = context.lowLevel().getPoseStack();

        for (DebugRenderState debugRenderState : debugRenderStateList) {
            poseStack.pushPose();
            LevelRenderer.renderLineBox(context.lowLevel().getPoseStack(),
                    vertexConsumer,
                    debugRenderState.AABBMinX,
                    debugRenderState.AABBMinY,
                    debugRenderState.AABBMinZ,
                    debugRenderState.AABBMaxX,
                    debugRenderState.AABBMaxY,
                    debugRenderState.AABBMaxZ,
                    1.0f, 1.0f, 1.0f, 0.5f
            );
            poseStack.popPose();

            float r = debugRenderState.isActive ? 0.3f : 0.6f;
            float g = debugRenderState.isActive ? 1.0f : 0.6f;
            float b = debugRenderState.isActive ? 0.3f : 0.6f;

            poseStack.pushPose();
            Vec3d pos = debugRenderState.renderPose.getPosition();
            poseStack.translate(pos.getX() - debugRenderState.camera.getX(), pos.getY() - debugRenderState.camera.getY(), pos.getZ() - debugRenderState.camera.getZ());
            poseStack.mulPose(Conversions.toJoml(debugRenderState.renderPose.getRotation()));
            PoseStack.Pose last = poseStack.last();

            for (int i = 0; i < debugRenderState.shape.length; i += 9) {
                line(vertexConsumer, last, debugRenderState.shape, i, i + 3, r, g, b);
                line(vertexConsumer, last, debugRenderState.shape, i + 3, i + 6, r, g, b);
                line(vertexConsumer, last, debugRenderState.shape, i + 6, i, r, g, b);
            }

            poseStack.popPose();

        }

        bufferSource.endBatch(RenderType.lines());
    }

    private static void line(VertexConsumer lines, PoseStack.Pose pose, float[] points, int from, int to, float r, float g, float b) {
        float x1 = points[from], y1 = points[from + 1], z1 = points[from + 2];
        float x2 = points[to], y2 = points[to + 1], z2 = points[to + 2];
        float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1e-6f) {
            return;
        }

        lines.addVertex(pose, x1, y1, z1).setColor(r, g, b, 1.0f).setNormal(pose, dx / length, dy / length, dz / length);
        lines.addVertex(pose, x2, y2, z2).setColor(r, g, b, 1.0f).setNormal(pose, dx / length, dy / length, dz / length);
    }

}
