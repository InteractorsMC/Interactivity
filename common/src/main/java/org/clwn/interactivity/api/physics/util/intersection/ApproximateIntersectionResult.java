package org.clwn.interactivity.api.physics.util.intersection;

import com.github.stephengold.joltjni.readonly.RVec3Arg;
import com.github.stephengold.joltjni.readonly.Vec3Arg;
import org.clwn.interactivity.api.physics.body.Body;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public interface ApproximateIntersectionResult {
    int getHitCount();

    List<Hit> getHits();

    interface Hit {
        Body getBody();

        RVec3Arg getPosition();

        Vec3Arg getNormal();

        float getFraction();

        @ApiStatus.Experimental
        int getJoltBodyId();
    }
}
