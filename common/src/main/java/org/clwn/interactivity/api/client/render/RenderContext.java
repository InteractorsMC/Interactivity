/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.client.render;


import org.jetbrains.annotations.ApiStatus;

public interface RenderContext {
    /**
     * Provides a high-level rendering context for rendering operations.
     * @return a high-level rendering context
     */
    HighLevelRenderContext highLevel();

    /**
     * Provides a low-level rendering context for rendering operations.
     * <p>
     *     Please note that this API is not version-independent, and it's context gonna vary between different game versions.
     *     If you want to render primitives such as blocks, items, entities, etc., consider using the high-level rendering context instead.
     * </p>
     * @return a low-level rendering context
     */
    @ApiStatus.Experimental
    LowLevelRenderContext lowLevel();
}
