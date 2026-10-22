/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.world;

import lombok.Getter;
import net.minecraft.world.level.Level;
import org.clwn.interactivity.api.physics.body.PhysicsStateAccessor;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;
import org.clwn.interactivity.core.physics.body.AbstractBodyManager;

public abstract class PhysicsWorldImpl implements PhysicsWorld {
    @Getter
    protected volatile int tick;

    @Getter
    protected volatile boolean isRunning;

    @Getter
    protected volatile boolean rollingBack;

    @Getter
    protected final Level level;

    public PhysicsWorldImpl(Level level) {
        this.level = level;
    }

    public abstract void init();

    public abstract void close();

    public abstract AbstractBodyManager getBodyManager();

    @Override
    public PhysicsStateAccessor getPhysicsStateAccessor() {
        return getBodyManager().getBodyDataStore();
    }
}
