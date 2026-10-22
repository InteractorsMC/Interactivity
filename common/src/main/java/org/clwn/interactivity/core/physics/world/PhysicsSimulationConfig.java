package org.clwn.interactivity.core.physics.world;

import org.clwn.interactivity.core.config.InteractivityConfig;
import org.clwn.interactivity.core.network.packet.s2c.S2CPhysicsConfigPacket;
import org.clwn.interactivity.core.physics.PhysicsLayerRegistryImpl;

public record PhysicsSimulationConfig(
        int tickRate,
        PhysicsLayerRegistryImpl physicsLayerRegistry,
        int maxBodies,
        int maxBodyPairs,
        int maxContactConstraints,
        int numPositionIterations,
        int numVelocityIterations,
        float speculativeContactDistance,
        float baumgarteFactor,
        float penetrationSlop,
        float timeBeforeSleep,
        float pointVelocitySleepThreshold,
        float gravityY,
        int tempAllocatorSize
) {
    public PhysicsSimulationConfig(PhysicsLayerRegistryImpl physicsLayerRegistry) {
        this(
                InteractivityConfig.physicsTickRate,
                physicsLayerRegistry,
                65536,
                65536,
                65536,
                10,
                15,
                0.02f,
                0.2f,
                0.02f,
                1.0f,
                0.005f,
                -9.81f,
                64 * 1024 * 1024
        );
    }

    public PhysicsSimulationConfig(PhysicsLayerRegistryImpl physicsLayerRegistry, S2CPhysicsConfigPacket syncPacket, int tempAllocatorSize) {
        this(
                syncPacket.tickRate(),
                physicsLayerRegistry,
                syncPacket.maxBodies(),
                syncPacket.maxBodyPairs(),
                syncPacket.maxContactConstraints(),
                syncPacket.numPositionIterations(),
                syncPacket.numVelocityIterations(),
                syncPacket.speculativeContactDistance(),
                syncPacket.baumgarteFactor(),
                syncPacket.penetrationSlop(),
                syncPacket.timeBeforeSleep(),
                syncPacket.pointVelocitySleepThreshold(),
                syncPacket.gravityY(),
                tempAllocatorSize
        );
    }
}