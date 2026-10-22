/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.client.physics.world;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.clwn.interactivity.api.client.physics.world.EstimatedClientPhysicsWorld;
import org.clwn.interactivity.core.client.physics.ClientPhysicsClock;
import org.clwn.interactivity.core.client.physics.body.EstimatedClientBodyManager;
import org.clwn.interactivity.core.physics.world.PhysicsWorldImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Client world that does not simulate anything.
 */
public class EstimatedClientPhysicsWorldImpl extends PhysicsWorldImpl implements EstimatedClientPhysicsWorld, ClientPhysicsWorldInternal {
    @Getter
    private final ClientLevel clientLevel;

    @Getter
    private final EstimatedClientBodyManager bodyManager;

    @Getter
    private final ClientPhysicsClock clock = new ClientPhysicsClock();

    public EstimatedClientPhysicsWorldImpl(ClientLevel level) {
        super(level);
        this.clientLevel = level;
        this.bodyManager = new EstimatedClientBodyManager(this);
    }

    @Override
    public void init() {
        this.isRunning = true;
    }

    @Override
    public void close() {
        this.isRunning = false;
        this.bodyManager.getBodyDataStore().clear();
    }

    public void onServerTick(int serverTick) {
        if (serverTick <= this.tick) {
            return;
        }

        this.tick = serverTick;
        this.clock.onPhysicsUpdate(serverTick);
    }

    /**
     * Runs the command on the client main thread, the only thread that touches this world.
     */
    @Override
    public void execute(@NotNull Runnable command) {
        Minecraft.getInstance().execute(command);
    }
}
