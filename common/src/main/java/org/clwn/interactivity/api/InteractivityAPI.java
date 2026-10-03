package org.clwn.interactivity.api;

import org.clwn.interactivity.api.network.NetworkManager;
import org.clwn.interactivity.api.physics.PhysicsPipeline;
import org.clwn.interactivity.api.physics.body.BodyTypeRegistry;
import org.clwn.interactivity.core.InteractivityAPIImpl;

public interface InteractivityAPI {
    static InteractivityAPI getInstance() {
        return InteractivityAPIImpl.INSTANCE;
    }

    PhysicsPipeline getPhysicsPipeline();

    BodyTypeRegistry getBodyTypeRegistry();

    NetworkManager getNetworkManager();
}
