/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.event.render;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import org.clwn.interactivity.core.event.Event;
import org.joml.Matrix4f;

public class LevelRenderEvents {
    public static final Event<LevelRenderContext> AFTER_ENTITIES = new Event<>();

    public record LevelRenderContext(LevelRenderer levelRenderer, GameRenderer gameRenderer, Camera camera, Matrix4f projectionMatrix, DeltaTracker deltaTracker) {

    }
}
