/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.clwn.interactivity.core.physics.world.PhysicsSimulationConfig;
import org.clwn.interactivity.core.util.InteractivityIdentifier;
import org.jetbrains.annotations.NotNull;

public record S2CPhysicsConfigPacket(
        int tickRate,
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
        float gravityY

) implements CustomPacketPayload {
    public static final Type<S2CPhysicsConfigPacket> TYPE =
            new Type<>(InteractivityIdentifier.getIdentifier("physics_config"));

    public static final StreamCodec<FriendlyByteBuf, S2CPhysicsConfigPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeVarInt(packet.tickRate);
                buf.writeVarInt(packet.maxBodies);
                buf.writeVarInt(packet.maxBodyPairs);
                buf.writeVarInt(packet.maxContactConstraints);
                buf.writeVarInt(packet.numPositionIterations);
                buf.writeVarInt(packet.numVelocityIterations);
                buf.writeFloat(packet.speculativeContactDistance);
                buf.writeFloat(packet.baumgarteFactor);
                buf.writeFloat(packet.penetrationSlop);
                buf.writeFloat(packet.timeBeforeSleep);
                buf.writeFloat(packet.pointVelocitySleepThreshold);
                buf.writeFloat(packet.gravityY);
            },
            buf -> new S2CPhysicsConfigPacket(
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat()
            )
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public S2CPhysicsConfigPacket(PhysicsSimulationConfig physicsSimulationConfig) {
        this(
                physicsSimulationConfig.tickRate(),
                physicsSimulationConfig.maxBodies(),
                physicsSimulationConfig.maxBodyPairs(),
                physicsSimulationConfig.maxContactConstraints(),
                physicsSimulationConfig.numPositionIterations(),
                physicsSimulationConfig.numVelocityIterations(),
                physicsSimulationConfig.speculativeContactDistance(),
                physicsSimulationConfig.baumgarteFactor(),
                physicsSimulationConfig.penetrationSlop(),
                physicsSimulationConfig.timeBeforeSleep(),
                physicsSimulationConfig.pointVelocitySleepThreshold(),
                physicsSimulationConfig.gravityY()
        );
    }
}
