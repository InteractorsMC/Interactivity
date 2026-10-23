/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.api.physics.world;

import net.minecraft.world.level.Level;
import org.clwn.interactivity.api.physics.body.PhysicsStateAccessor;
import org.clwn.interactivity.api.util.Side;

import java.util.concurrent.Executor;

public interface PhysicsWorld extends Executor {
    /**
     * Returns current physics tick
     * <p>
     * For non-simulated worlds this is the latest tick received from the server.
     *
     * @return current physics tick
     */
    int getTick();

    boolean isSimulated();

    boolean isRunning();

    Level getLevel();

    PhysicsStateAccessor getPhysicsStateAccessor();

    Side getSide();

}
