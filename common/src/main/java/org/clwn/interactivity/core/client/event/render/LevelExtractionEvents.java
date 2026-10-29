/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.client.event.render;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import org.clwn.interactivity.core.event.internal.Event;

public class LevelExtractionEvents {
    public static final Event<LevelExtractionContext> EXTRACTION_END = new Event<>();

    public record LevelExtractionContext(LevelRenderer levelRenderer, Camera camera, Frustum frustum) {
    }
}
