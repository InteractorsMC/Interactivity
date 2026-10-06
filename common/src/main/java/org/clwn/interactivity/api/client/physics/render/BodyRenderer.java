/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.client.physics.render;

import com.github.stephengold.joltjni.Body;
import org.clwn.interactivity.api.client.render.ExtractionContext;
import org.clwn.interactivity.api.client.render.RenderContext;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;

public interface BodyRenderer<S> {
    S createState();

    void extract(
            ExtractionContext context,
            PhysicsWorld physicsWorld,
            Body body,
            S state
    );

    void submit(
            S state,
            RenderContext context
    );
}