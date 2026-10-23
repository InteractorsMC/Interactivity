package org.clwn.interactivity.api.client.physics;

import org.clwn.interactivity.api.client.physics.world.ClientPhysicsWorld;

public interface ClientPhysicsPipeline {
    ClientPhysicsWorld getCurrentPhysicsWorld();
}
