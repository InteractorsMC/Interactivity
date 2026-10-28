/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.persistence;

import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * Saved bodies that are connected by constraints, always loaded and unloaded together.
 *
 * @param footprint   {@link net.minecraft.world.level.ChunkPos#asLong} of the chunk of every body, without duplicates.
 *                    The group is loaded once all of them are loaded
 * @param bodies      the bodies
 * @param constraints the constraints between them
 */
public record GroupRecord(long[] footprint, BodyRecord[] bodies, ConstraintRecord[] constraints) {
    /**
     * @param position        origin of the body
     * @param rotation        x, y, z, w
     * @param linearVelocity  x, y, z
     * @param angularVelocity x, y, z
     * @param awake           {@code false} if the body was asleep
     */
    public record BodyRecord(
            UUID uuid,
            ResourceLocation type,
            double[] position,
            float[] rotation,
            float[] linearVelocity,
            float[] angularVelocity,
            boolean awake
    ) {
    }

    /**
     * @param body1    index of the first body in {@link GroupRecord#bodies}
     * @param body2    index of the second body, or {@link #WORLD} if the constraint is attached to the world
     * @param settings Jolt object stream (text) of the settings of the live constraint
     * @param state    Jolt state recorder data of the constraint, may be empty
     */
    public record ConstraintRecord(UUID uuid, int body1, int body2, byte[] settings, byte[] state) {
        public static final int WORLD = -1;
    }
}
