/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.util.network;

import com.github.stephengold.joltjni.Quat;
import com.github.stephengold.joltjni.RVec3;
import com.github.stephengold.joltjni.Vec3;
import com.github.stephengold.joltjni.readonly.QuatArg;
import com.github.stephengold.joltjni.readonly.RVec3Arg;
import com.github.stephengold.joltjni.readonly.Vec3Arg;
import net.minecraft.network.FriendlyByteBuf;

public final class InteractivityCodecs {
    private InteractivityCodecs() {
    }


    public static void writeVec3(FriendlyByteBuf buf, Vec3Arg value) {
        buf.writeFloat(value.getX());
        buf.writeFloat(value.getY());
        buf.writeFloat(value.getZ());
    }

    public static Vec3 readVec3(FriendlyByteBuf buf) {
        return readVec3(buf, new Vec3());
    }

    public static Vec3 readVec3(FriendlyByteBuf buf, Vec3 out) {
        out.set(buf.readFloat(), buf.readFloat(), buf.readFloat());
        return out;
    }

    public static void writeRVec3(FriendlyByteBuf buf, RVec3Arg value) {
        buf.writeDouble(value.xx());
        buf.writeDouble(value.yy());
        buf.writeDouble(value.zz());
    }

    public static RVec3 readRVec3(FriendlyByteBuf buf) {
        return readRVec3(buf, new RVec3());
    }

    public static RVec3 readRVec3(FriendlyByteBuf buf, RVec3 out) {
        out.set(buf.readDouble(), buf.readDouble(), buf.readDouble());
        return out;
    }

    public static void writeQuat(FriendlyByteBuf buf, QuatArg value) {
        buf.writeFloat(value.getX());
        buf.writeFloat(value.getY());
        buf.writeFloat(value.getZ());
        buf.writeFloat(value.getW());
    }

    public static Quat readQuat(FriendlyByteBuf buf) {
        return readQuat(buf, new Quat());
    }

    public static Quat readQuat(FriendlyByteBuf buf, Quat out) {
        out.set(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
        return out;
    }
}
