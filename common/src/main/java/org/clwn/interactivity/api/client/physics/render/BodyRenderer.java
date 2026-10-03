package org.clwn.interactivity.api.client.physics.render;

import org.clwn.interactivity.api.client.render.ExtractionContext;
import org.clwn.interactivity.api.client.render.RenderContext;

public interface BodyRenderer<S> {
    void extract(
            ExtractionContext context,
            int bodyIndex,
            S state
    );

    void submit(
            S state,
            RenderContext context
    );
}