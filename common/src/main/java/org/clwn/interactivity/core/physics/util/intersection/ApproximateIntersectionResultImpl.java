package org.clwn.interactivity.core.physics.util.intersection;

import com.github.stephengold.joltjni.readonly.RVec3Arg;
import com.github.stephengold.joltjni.readonly.Vec3Arg;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.util.intersection.ApproximateIntersectionResult;

import java.util.List;

@AllArgsConstructor
public class ApproximateIntersectionResultImpl implements ApproximateIntersectionResult {
    @Getter
    private final int hitCount;

    @Getter
    private final List<Hit> hits;

    @AllArgsConstructor
    public static class HitImpl implements Hit {
        @Getter
        private final Body body;

        @Getter
        private final RVec3Arg position;

        @Getter
        private final Vec3Arg normal;

        @Getter
        private final float fraction;

        @Getter
        private final int joltBodyId;

    }
}
