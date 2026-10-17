/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.client.physics.render;

import net.minecraft.resources.ResourceLocation;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyType;


public interface BodyRendererRegistry {
    <B extends Body, R extends BodyRenderer<?>> void register(
            BodyType<B> bodyType,
            R renderer
    );

    default <B extends Body> BodyRenderer<?> getRenderer(BodyType<B> bodyType) {
        return getRenderer(bodyType.getTypeId());
    }

    BodyRenderer<?> getRenderer(ResourceLocation bodyTypeId);
}
