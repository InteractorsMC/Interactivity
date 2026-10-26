/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.command.argument;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.core.physics.body.store.PhysicsStateContainer;
import org.clwn.interactivity.core.physics.body.store.SimulatedBodyDataStore;
import org.clwn.interactivity.core.physics.world.SimulatedPhysicsWorldImpl;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Parsed {@code @b[...]} selector or a single body UUID.
 *
 * @param uuid         selects exactly this body, all other filters are ignored
 * @param type         body type filter, {@code null} for any type
 * @param typeInverted {@code true} if the type filter was written as {@code type=!<id>}
 * @param distance     distance from the command origin
 * @param limit        maximum number of selected bodies
 * @param sort         order in which bodies are picked before the limit is applied
 */
public record BodySelector(
        @Nullable UUID uuid,
        @Nullable ResourceLocation type,
        boolean typeInverted,
        MinMaxBounds.Doubles distance,
        int limit,
        Sort sort
) {
    /**
     * Selects the bodies of a world. Reads the data store, so it has to run on the physics thread.
     */
    public List<Body> select(SimulatedPhysicsWorldImpl world, Vec3 origin) {
        SimulatedBodyDataStore store = world.getBodyManager().getBodyDataStore();
        PhysicsStateContainer container = store.getPhysicsStateContainer();

        if (uuid != null) {
            int index = store.getIndexByUuid(uuid);
            return index == -1 ? List.of() : List.of(container.bodies[index]);
        }

        ObjectArrayList<Candidate> candidates = new ObjectArrayList<>();
        for (int i = 0; i < store.getSlotCount(); i++) {
            Body body = container.bodies[i];
            if (body == null) {
                continue;
            }
            if (type != null && body.getType().getTypeId().equals(type) == typeInverted) {
                continue;
            }

            double dx = container.posX.get(i) - origin.x;
            double dy = container.posY.get(i) - origin.y;
            double dz = container.posZ.get(i) - origin.z;
            double distanceSqr = dx * dx + dy * dy + dz * dz;
            if (!distance.matchesSqr(distanceSqr)) {
                continue;
            }

            candidates.add(new Candidate(body, distanceSqr));
        }

        switch (sort) {
            case NEAREST -> candidates.sort(Comparator.comparingDouble(Candidate::distanceSqr));
            case FURTHEST -> candidates.sort(Comparator.comparingDouble(Candidate::distanceSqr).reversed());
            case RANDOM -> Collections.shuffle(candidates);
            case ARBITRARY -> {}
        }

        int count = Math.min(limit, candidates.size());
        ObjectArrayList<Body> result = new ObjectArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(candidates.get(i).body());
        }
        return result;
    }

    private record Candidate(Body body, double distanceSqr) {
    }

    public enum Sort {
        NEAREST,
        FURTHEST,
        RANDOM,
        ARBITRARY;

        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static @Nullable Sort byName(String name) {
            for (Sort sort : values()) {
                if (sort.getName().equals(name)) {
                    return sort;
                }
            }
            return null;
        }
    }
}
