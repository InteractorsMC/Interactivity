package org.clwn.interactivity.api.client.physics.render;

import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyType;


public interface BodyRendererRegistry {
    <T extends Body> void register(
            BodyType<T> bodyType,
            Class<BodyRenderer<T>> factory
    );

    <T extends Body> BodyRenderer<T> getRenderer(BodyType<T> bodyType);
}
