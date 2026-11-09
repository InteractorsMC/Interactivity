/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.util.intersection;

import com.github.stephengold.joltjni.*;
import com.github.stephengold.joltjni.readonly.RVec3Arg;
import net.minecraft.world.phys.AABB;
import org.clwn.interactivity.api.physics.filter.BroadPhaseLayerFilter;
import org.clwn.interactivity.api.physics.filter.ObjectLayerFilter;
import org.jetbrains.annotations.ApiStatus;

/**
 * Checks for finding intersections between shapes and world objects.
 */
public interface ApproximateIntersector {
    ApproximateIntersectionResult castRay(
            RVec3Arg origin,
            Vec3 direction,
            float maxDistance,
            BroadPhaseLayerFilter bpFilter,
            ObjectLayerFilter olFilter,
            HitCollectMode collectMode
    );

    @ApiStatus.Experimental
    ApproximateIntersectionResult castRay(
            RRayCast raycast,
            RayCastSettings settings,
            com.github.stephengold.joltjni.BroadPhaseLayerFilter bplFilter,
            com.github.stephengold.joltjni.ObjectLayerFilter olFilter,
            BodyFilter bodyFilter,
            ShapeFilter shapeFilter,
            HitCollectMode collectMode
    );

    ApproximateIntersectionResult castBox(
            AABB aabb,
            BroadPhaseLayerFilter bpFilter,
            ObjectLayerFilter olFilter,
            HitCollectMode collectMode
    );

    @ApiStatus.Experimental
    ApproximateIntersectionResult castBox(
            AaBoxCast boxCast,
            com.github.stephengold.joltjni.BroadPhaseLayerFilter bplFilter,
            com.github.stephengold.joltjni.ObjectLayerFilter olFilter,
            BodyFilter bodyFilter,
            ShapeFilter shapeFilter,
            HitCollectMode collectMode
    );

    ApproximateIntersectionResult collidePoint(
            RVec3Arg point,
            BroadPhaseLayerFilter bpFilter,
            ObjectLayerFilter olFilter,
            HitCollectMode collectMode
    );

    @ApiStatus.Experimental
    ApproximateIntersectionResult collidePoint(
            RVec3Arg point,
            com.github.stephengold.joltjni.BroadPhaseLayerFilter bplFilter,
            com.github.stephengold.joltjni.ObjectLayerFilter olFilter,
            BodyFilter bodyFilter,
            ShapeFilter shapeFilter,
            HitCollectMode collectMode
    );
}
