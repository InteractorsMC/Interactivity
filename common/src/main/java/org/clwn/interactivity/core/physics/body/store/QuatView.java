package org.clwn.interactivity.core.physics.body.store;

import org.clwn.interactivity.api.util.math.Quat;

import java.nio.FloatBuffer;

public final class QuatView implements Quat {
    private final FloatBuffer xBuffer;
    private final FloatBuffer yBuffer;
    private final FloatBuffer zBuffer;
    private final FloatBuffer wBuffer;

    private final int index;

    public QuatView(
            FloatBuffer xBuffer,
            FloatBuffer yBuffer,
            FloatBuffer zBuffer,
            FloatBuffer wBuffer,
            int index
    ) {
        this.xBuffer = xBuffer;
        this.yBuffer = yBuffer;
        this.zBuffer = zBuffer;
        this.wBuffer = wBuffer;
        this.index = index;
    }

    @Override
    public float getX() {
        return xBuffer.get(index);
    }

    @Override
    public float getY() {
        return yBuffer.get(index);
    }

    @Override
    public float getZ() {
        return zBuffer.get(index);
    }

    @Override
    public float getW() {
        return wBuffer.get(index);
    }
}
