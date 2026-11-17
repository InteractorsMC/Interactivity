/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.natives;

import java.nio.ByteBuffer;

/**
 * JNI binding for the native contact listener manager of a physics system.
 * <p>
 * The manager is the {@code JPH::ContactListener} of the system and forwards contacts to its native listeners:
 * Body Pair Ignore rejects contacts between ignored pairs, Contact Notifier collects contacts that match a notifier.
 * <p>
 * The low 16 bits of a body's user data are its body type id, {@link #BODY_TYPE_TERRAIN} for terrain.
 * Body ids are full Jolt ids (index and sequence number).
 * <p>
 * Calls must be made from the physics thread while {@code PhysicsSystem#update} is not running.
 */
public final class ContactNative {
    /** Wildcard body id / type id of a notifier */
    public static final int ANY = 0xFFFFFFFF;

    public static final int BODY_TYPE_TERRAIN = 0xFFFF;

    public static final int KIND_ADDED = 1;
    public static final int KIND_PERSISTED = 2;
    public static final int KIND_REMOVED = 4;

    /** Event records, in native byte order. Body 1 is the side the notifier matched, the normal points to body 2 */
    public static final int RECORD_SIZE = 72;
    public static final int RECORD_POINT = 0;
    public static final int RECORD_NORMAL = 24;
    public static final int RECORD_PENETRATION = 36;
    public static final int RECORD_APPROACH_SPEED = 40;
    public static final int RECORD_NOTIFIER_ID = 44;
    public static final int RECORD_BODY_1 = 48;
    public static final int RECORD_BODY_2 = 52;
    public static final int RECORD_SUB_SHAPE_USER_DATA_1 = 56;
    public static final int RECORD_SUB_SHAPE_USER_DATA_2 = 60;
    public static final int RECORD_TYPE_2 = 64;
    public static final int RECORD_KIND = 68;

    private ContactNative() {}

    /**
     * Creates a manager and sets it as the contact listener of the physics system.
     *
     * @return the manager handle
     */
    public static native long create(long physicsSystemVa);

    public static native void destroy(long handle);

    /**
     * Body 1 / type 1 select the body the events are about, body 2 / type 2 the body it touched, {@link #ANY} matches any.
     *
     * @param kinds            a mask of {@code KIND_*}
     * @param minApproachSpeed only for added and persisted contacts
     */
    public static native void addNotifier(long handle, int id, int kinds, int body1, int type1, int body2, int type2, float minApproachSpeed);

    public static native void removeNotifier(long handle, int id);

    /**
     * Rejects all contacts between two bodies, including ones that are touching already.
     */
    public static native void ignorePair(long handle, int body1, int body2);

    public static native void unignorePair(long handle, int body1, int body2);

    /**
     * Removes the ignored pairs of a body and the notifiers bound to it. Must be called before the body is destroyed.
     */
    public static native void removeBody(long handle, int body);

    /**
     * @return a direct buffer over the event records of the last update, or {@code null} when there are none.
     * Valid until {@link #clearEvents(long)}.
     */
    public static native ByteBuffer getEvents(long handle);

    public static native void clearEvents(long handle);
}
