/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.natives;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * JNI binding for building static terrain bodies out of axis aligned boxes.
 * <p>
 * Boxes are stored as 6 floats each ({@code minX, minY, minZ, maxX, maxY, maxZ}) relative to the body origin.
 * Every box also has an {@code int} tag, which becomes the user data of its sub shape in the compound shape,
 * so contacts can find out what kind of block they touched.
 * <p>
 * All buffers must be direct and use native byte order; indices are absolute, buffer positions are ignored.
 * Calls must be made from the physics thread while {@code PhysicsSystem#update} is not running.
 * Every created, reshaped or removed body wakes up the surrounding bodies, because changing a static body does not.
 */
public final class TerrainNative {
    private TerrainNative() {}

    /**
     * Creates static bodies with one compound shape each and adds them to the physics system in a single batch.
     *
     * @param physicsSystemVa native address of the {@code JPH::PhysicsSystem}
     * @param objectLayer     object layer of the new bodies
     * @param bodyCount       number of bodies to create
     * @param origins         3 doubles per body, its world position
     * @param boxStarts       per body, index of its first box in {@code boxes}/{@code tags}
     * @param boxCounts       per body, number of its boxes (at least 1)
     * @param boxes           6 floats per box
     * @param tags            1 int per box
     * @param outBodyIds      receives the id of every body, or {@code 0xFFFFFFFF} when it could not be created
     * @return number of bodies that were created
     */
    public static native int createTerrainBodies(
            long physicsSystemVa,
            int objectLayer,
            int bodyCount,
            DoubleBuffer origins,
            IntBuffer boxStarts,
            IntBuffer boxCounts,
            FloatBuffer boxes,
            IntBuffer tags,
            IntBuffer outBodyIds
    );

    /**
     * Replaces the shape of a terrain body.
     *
     * @param boxCount at least 1
     * @return false when the shape could not be built; the body keeps its old shape then
     */
    public static native boolean setTerrainShape(
            long physicsSystemVa,
            int bodyId,
            FloatBuffer boxes,
            IntBuffer tags,
            int boxStart,
            int boxCount
    );

    /**
     * Removes and destroys terrain bodies.
     *
     * @param wake whether bodies around the removed ones are woken up so they can fall
     */
    public static native void removeTerrainBodies(
            long physicsSystemVa,
            IntBuffer bodyIds,
            int count,
            boolean wake
    );
}
