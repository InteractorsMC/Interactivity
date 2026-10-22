/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.network;

import net.minecraft.network.FriendlyByteBuf;
import org.clwn.interactivity.core.physics.body.store.PhysicsStateContainer;

/**
 * Wire format of the state of one body, shared by spawn and state packets.
 */
public final class BodyStateCodec {
    private static final int FLAG_ACTIVE = 1;

    private BodyStateCodec() {}

    /**
     * Writes the state of the body in slot {@code index}, including its network id.
     */
    public static void write(FriendlyByteBuf buf, PhysicsStateContainer container, int index) {
        boolean active = container.isActive.get(index) != 0;

        buf.writeVarInt(container.networkId.get(index));
        buf.writeByte(active ? FLAG_ACTIVE : 0);

        buf.writeDouble(container.posX.get(index));
        buf.writeDouble(container.posY.get(index));
        buf.writeDouble(container.posZ.get(index));

        buf.writeFloat(container.rotX.get(index));
        buf.writeFloat(container.rotY.get(index));
        buf.writeFloat(container.rotZ.get(index));
        buf.writeFloat(container.rotW.get(index));

        buf.writeFloat(container.aabbMinX.get(index));
        buf.writeFloat(container.aabbMinY.get(index));
        buf.writeFloat(container.aabbMinZ.get(index));
        buf.writeFloat(container.aabbMaxX.get(index));
        buf.writeFloat(container.aabbMaxY.get(index));
        buf.writeFloat(container.aabbMaxZ.get(index));

        if (active) {
            buf.writeFloat(container.velX.get(index));
            buf.writeFloat(container.velY.get(index));
            buf.writeFloat(container.velZ.get(index));
            buf.writeFloat(container.angVelX.get(index));
            buf.writeFloat(container.angVelY.get(index));
            buf.writeFloat(container.angVelZ.get(index));
        }
    }

    /**
     * A decoded state. Meant to be reused for all records of a packet to avoid allocations.
     */
    public static final class Record {
        public int networkId;
        public boolean active;
        public double x, y, z;
        public float qx, qy, qz, qw;
        public float aabbMinX, aabbMinY, aabbMinZ, aabbMaxX, aabbMaxY, aabbMaxZ;
        public float vx, vy, vz, wx, wy, wz;

        public void read(FriendlyByteBuf buf) {
            networkId = buf.readVarInt();
            active = (buf.readByte() & FLAG_ACTIVE) != 0;

            x = buf.readDouble();
            y = buf.readDouble();
            z = buf.readDouble();

            qx = buf.readFloat();
            qy = buf.readFloat();
            qz = buf.readFloat();
            qw = buf.readFloat();

            aabbMinX = buf.readFloat();
            aabbMinY = buf.readFloat();
            aabbMinZ = buf.readFloat();
            aabbMaxX = buf.readFloat();
            aabbMaxY = buf.readFloat();
            aabbMaxZ = buf.readFloat();

            if (active) {
                vx = buf.readFloat();
                vy = buf.readFloat();
                vz = buf.readFloat();
                wx = buf.readFloat();
                wy = buf.readFloat();
                wz = buf.readFloat();
            } else {
                vx = vy = vz = wx = wy = wz = 0f;
            }
        }

        /**
         * Stores this state into slot {@code index}. Does not touch the network id or the tick.
         */
        public void applyTo(PhysicsStateContainer container, int index) {
            container.isActive.put(index, (byte) (active ? 1 : 0));

            container.posX.put(index, x);
            container.posY.put(index, y);
            container.posZ.put(index, z);

            container.rotX.put(index, qx);
            container.rotY.put(index, qy);
            container.rotZ.put(index, qz);
            container.rotW.put(index, qw);

            container.aabbMinX.put(index, aabbMinX);
            container.aabbMinY.put(index, aabbMinY);
            container.aabbMinZ.put(index, aabbMinZ);
            container.aabbMaxX.put(index, aabbMaxX);
            container.aabbMaxY.put(index, aabbMaxY);
            container.aabbMaxZ.put(index, aabbMaxZ);

            container.velX.put(index, vx);
            container.velY.put(index, vy);
            container.velZ.put(index, vz);
            container.angVelX.put(index, wx);
            container.angVelY.put(index, wy);
            container.angVelZ.put(index, wz);
        }
    }
}
