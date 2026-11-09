package org.clwn.interactivity.core.physics.util.intersection;

import com.github.stephengold.joltjni.*;
import com.github.stephengold.joltjni.operator.Op;
import com.github.stephengold.joltjni.readonly.RVec3Arg;
import org.clwn.interactivity.api.physics.filter.BroadPhaseLayerFilter;
import org.clwn.interactivity.api.physics.filter.ObjectLayerFilter;
import org.clwn.interactivity.api.physics.util.intersection.HitCollectMode;
import org.clwn.interactivity.api.physics.util.intersection.ApproximateIntersectionResult;
import org.clwn.interactivity.api.physics.util.intersection.Intersector;
import org.clwn.interactivity.core.physics.world.SimulatedPhysicsWorldImpl;

import java.util.ArrayList;
import java.util.List;

public class ApproximateIntersector implements Intersector {
    private final SimulatedPhysicsWorldImpl physicsWorld;

    public ApproximateIntersector(SimulatedPhysicsWorldImpl physicsWorld) {
        this.physicsWorld = physicsWorld;
    }

    @Override
    public ApproximateIntersectionResult castRay(RVec3Arg origin, Vec3 direction, float maxDistance, BroadPhaseLayerFilter bpFilter, ObjectLayerFilter olFilter, HitCollectMode collectMode) {
        try (RRayCast ray = new RRayCast(origin, Op.star(direction, maxDistance));
             RayCastSettings settings = new RayCastSettings()) {
            return castRay(ray, settings, bpFilter.getFilter(), olFilter.getFilter(), DEFAULT_BODY_FILTER, DEFAULT_SHAPE_FILTER, collectMode);
        }
    }

    @Override
    public ApproximateIntersectionResult castRay(RRayCast raycast, RayCastSettings settings, com.github.stephengold.joltjni.BroadPhaseLayerFilter bplFilter, com.github.stephengold.joltjni.ObjectLayerFilter olFilter, BodyFilter bodyFilter, ShapeFilter shapeFilter, HitCollectMode collectMode) {
        CastRayCollector collector;

        switch (collectMode) {
            case ALL -> collector = new AllHitCastRayCollector();
            case CLOSEST -> collector = new ClosestHitCastRayCollector();
            case ANY -> collector = new AnyHitCastRayCollector();
            default -> {
                return null;
            }
        }

        physicsWorld.getJoltBridge().getBroadPhaseQuery().castRay(
                raycast,
                collector,
                bplFilter,
                olFilter
        );

        int hitCount;
        List<RayCastResult> rawRayCastResults;

        if (collector instanceof AllHitCastRayCollector allHitCastRayCollector) {
            hitCount = allHitCastRayCollector.countHits();
            rawRayCastResults = allHitCastRayCollector.getHits();
        } else if (collector instanceof ClosestHitCastRayCollector closestHitCastRayCollector) {
            hitCount = closestHitCastRayCollector.hadHit() ? 1 : 0;
            rawRayCastResults = List.of(closestHitCastRayCollector.getHit());
        } else if (collector instanceof AnyHitCastRayCollector anyHitCastRayCollector) {
            hitCount = anyHitCastRayCollector.hadHit() ? 1 : 0;
            rawRayCastResults = List.of(anyHitCastRayCollector.getHit());
        } else {
            return null;
        }

        List<ApproximateIntersectionResult.Hit> hits = new ArrayList<>(hitCount);

        for (RayCastResult rawHit : rawRayCastResults) {
            int bodyId = rawHit.getBodyId();
            float fraction = rawHit.getFraction();
            RVec3 hitPos = raycast.getPointOnRay(fraction);

            Vec3 normal;
            try (BodyLockRead lock = new BodyLockRead(physicsWorld.getJoltBridge().getBodyLockInterface(), bodyId)) {
                if (lock.succeededAndIsInBroadPhase()) {
                    normal = lock.getBody().getWorldSpaceSurfaceNormal(rawHit.getSubShapeId2(), hitPos);
                } else {
                    normal = null;
                }
            }


            ApproximateIntersectionResultImpl.HitImpl hit = new ApproximateIntersectionResultImpl.HitImpl(
                    physicsWorld.getBodyManager().getBodyByJoltId(bodyId),
                    hitPos,
                    normal,
                    fraction,
                    rawHit.getSubShapeId2(),
                    bodyId
            );
            hits.add(hit);
        }

        collector.close();

        return new ApproximateIntersectionResultImpl(hitCount, hits);
    }
}
