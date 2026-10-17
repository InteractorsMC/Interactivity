package org.clwn.interactivity.api.physics;

import lombok.Getter;

public enum BroadPhaseLayer {
    NON_MOVING(0),
    MOVING(1);

    @Getter
    private final int value;

    BroadPhaseLayer(int value) {
        this.value = value;
    }
}
