package org.clwn.interactivity.api.physics.util.intersection;

import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public interface PreciseIntersectionResult extends ApproximateIntersectionResult {
    int getHitCount();

    List<PreciseHit> getPreciseHits();

    interface PreciseHit extends Hit {
        /**
         * @return {@link com.github.stephengold.joltjni.Jolt#cEmptySubShapeId} if not applicable.
         */
        @ApiStatus.Experimental
        int getSubShapeID();
    }
}
