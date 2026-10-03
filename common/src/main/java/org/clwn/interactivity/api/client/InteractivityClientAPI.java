package org.clwn.interactivity.api.client;

import org.clwn.interactivity.api.client.physics.render.BodyRendererRegistry;
import org.clwn.interactivity.core.client.InteractivityClientAPIImpl;

public interface InteractivityClientAPI {
    static InteractivityClientAPI getInstance() {
        return InteractivityClientAPIImpl.INSTANCE;
    }

    BodyRendererRegistry getBodyRendererRegistry();
}
