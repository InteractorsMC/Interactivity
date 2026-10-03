package org.clwn.interactivity.core.client;

import org.clwn.interactivity.api.client.InteractivityClientAPI;
import org.clwn.interactivity.api.client.physics.render.BodyRendererRegistry;

public class InteractivityClientAPIImpl implements InteractivityClientAPI {
    public static final InteractivityClientAPIImpl INSTANCE = new InteractivityClientAPIImpl();

    private InteractivityClientAPIImpl() {

    }

    @Override
    public BodyRendererRegistry getBodyRendererRegistry() {
        return null;
    }
}
