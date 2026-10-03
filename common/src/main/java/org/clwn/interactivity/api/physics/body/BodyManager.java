package org.clwn.interactivity.api.physics.body;

import com.github.stephengold.joltjni.readonly.QuatArg;
import com.github.stephengold.joltjni.readonly.RVec3Arg;

public interface BodyManager {
    void spawnBody(Body body, RVec3Arg position, QuatArg rotation);
    void removeBody(Body body);
}
