/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.persistence;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;
import java.util.UUID;

/**
 * Binary format of the payload of one chunk (version 1):
 * <pre>
 * groupCount varint
 * group:     length varint + bytes (so a group can be kept as raw bytes)
 *   footprintCount varint, footprint: n x long
 *   bodyCount varint
 *   body:
 *     uuid            2 x long
 *     type            utf (resource location)
 *     position        3 x double
 *     rotation        4 x float
 *     linearVelocity  3 x float
 *     angularVelocity 3 x float
 *     flags           byte (bit 0 = awake)
 *     data            length varint + bytes
 *   constraintCount varint
 *   constraint:
 *     uuid            2 x long
 *     body1           varint (index in the bodies of the group)
 *     body2           varint (0 = world, else index + 1)
 *     settings        length varint + bytes
 *     state           length varint + bytes
 * </pre>
 */
public final class ChunkRecordCodec {
    private static final int FLAG_AWAKE = 1;

    private ChunkRecordCodec() {
    }

    /**
     * @return the payload, or {@code null} if there are no groups
     */
    public static byte[] encodeChunk(List<byte[]> groups) {
        if (groups.isEmpty()) {
            return null;
        }

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeVarInt(groups.size());
        for (byte[] group : groups) {
            buf.writeByteArray(group);
        }
        return toArray(buf);
    }

    /**
     * Splits a payload into the raw bytes of its groups.
     */
    public static List<byte[]> decodeChunk(byte[] payload) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload));

        int count = buf.readVarInt();
        List<byte[]> groups = new ObjectArrayList<>(count);
        for (int i = 0; i < count; i++) {
            groups.add(buf.readByteArray());
        }
        return groups;
    }

    public static byte[] encodeGroup(GroupRecord group) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        buf.writeVarInt(group.footprint().length);
        for (long chunk : group.footprint()) {
            buf.writeLong(chunk);
        }

        buf.writeVarInt(group.bodies().length);
        for (GroupRecord.BodyRecord body : group.bodies()) {
            buf.writeUUID(body.uuid());
            buf.writeResourceLocation(body.type());
            for (double value : body.position()) {
                buf.writeDouble(value);
            }
            writeFloats(buf, body.rotation());
            writeFloats(buf, body.linearVelocity());
            writeFloats(buf, body.angularVelocity());
            buf.writeByte(body.awake() ? FLAG_AWAKE : 0);
        }

        buf.writeVarInt(group.constraints().length);
        for (GroupRecord.ConstraintRecord constraint : group.constraints()) {
            buf.writeUUID(constraint.uuid());
            buf.writeVarInt(constraint.body1());
            buf.writeVarInt(constraint.body2() == GroupRecord.ConstraintRecord.WORLD ? 0 : constraint.body2() + 1);
            buf.writeByteArray(constraint.settings());
            buf.writeByteArray(constraint.state());
        }

        return toArray(buf);
    }

    /**
     * @throws RuntimeException if the group is corrupt
     */
    public static GroupRecord decodeGroup(byte[] raw) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(raw));

        long[] footprint = new long[readCount(buf)];
        for (int i = 0; i < footprint.length; i++) {
            footprint[i] = buf.readLong();
        }

        GroupRecord.BodyRecord[] bodies = new GroupRecord.BodyRecord[readCount(buf)];
        for (int i = 0; i < bodies.length; i++) {
            bodies[i] = new GroupRecord.BodyRecord(
                    buf.readUUID(),
                    buf.readResourceLocation(),
                    new double[]{buf.readDouble(), buf.readDouble(), buf.readDouble()},
                    readFloats(buf, 4),
                    readFloats(buf, 3),
                    readFloats(buf, 3),
                    (buf.readByte() & FLAG_AWAKE) != 0
            );
        }

        GroupRecord.ConstraintRecord[] constraints = new GroupRecord.ConstraintRecord[readCount(buf)];
        for (int i = 0; i < constraints.length; i++) {
            UUID uuid = buf.readUUID();
            int body1 = buf.readVarInt();
            int body2 = buf.readVarInt() - 1;
            if (body1 < 0 || body1 >= bodies.length || body2 < GroupRecord.ConstraintRecord.WORLD || body2 >= bodies.length) {
                throw new IllegalStateException("Constraint " + uuid + " references a missing body");
            }
            constraints[i] = new GroupRecord.ConstraintRecord(uuid, body1, body2, buf.readByteArray(), buf.readByteArray());
        }

        if (buf.isReadable()) {
            throw new IllegalStateException(buf.readableBytes() + " unexpected bytes at the end of a group");
        }

        return new GroupRecord(footprint, bodies, constraints);
    }

    private static int readCount(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        // Every element takes at least one byte
        if (count < 0 || count > buf.readableBytes()) {
            throw new IllegalStateException("Invalid count " + count);
        }
        return count;
    }

    private static void writeFloats(FriendlyByteBuf buf, float[] values) {
        for (float value : values) {
            buf.writeFloat(value);
        }
    }

    private static float[] readFloats(FriendlyByteBuf buf, int count) {
        float[] values = new float[count];
        for (int i = 0; i < count; i++) {
            values[i] = buf.readFloat();
        }
        return values;
    }

    private static byte[] toArray(FriendlyByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }
}
