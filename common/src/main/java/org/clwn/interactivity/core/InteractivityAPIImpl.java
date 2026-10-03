package org.clwn.interactivity.core;

import org.clwn.interactivity.api.InteractivityAPI;
import org.clwn.interactivity.api.network.NetworkManager;
import org.clwn.interactivity.api.physics.PhysicsPipeline;
import org.clwn.interactivity.api.physics.body.BodyTypeRegistry;
import org.clwn.interactivity.core.network.NetworkManagerImpl;
import org.clwn.interactivity.core.physics.PhysicsPipelineImpl;
import org.clwn.interactivity.core.physics.body.BodyTypeRegistryImpl;

public class InteractivityAPIImpl implements InteractivityAPI {
    public static final InteractivityAPI INSTANCE = new InteractivityAPIImpl();

    private InteractivityAPIImpl() {
    }


    @Override
    public PhysicsPipeline getPhysicsPipeline() {
        return PhysicsPipelineImpl.INSTANCE;
    }

    @Override
    public BodyTypeRegistry getBodyTypeRegistry() {
        return BodyTypeRegistryImpl.INSTANCE;
    }

    @Override
    public NetworkManager getNetworkManager() {
        return NetworkManagerImpl.INSTANCE;
    }
}
