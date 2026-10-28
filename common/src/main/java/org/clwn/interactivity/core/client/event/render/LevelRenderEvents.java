/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.client.event.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.clwn.interactivity.core.event.Event;
import org.joml.Matrix4f;

public class LevelRenderEvents {
    public static final Event<EntityPassContext> ENTITY_PASS = new Event<>();

    public enum Pass {
        MAIN,
        SECONDARY
    }

    public record EntityPassContext(Pass pass, PoseStack poseStack, MultiBufferSource bufferSource, Camera camera) {}
}
