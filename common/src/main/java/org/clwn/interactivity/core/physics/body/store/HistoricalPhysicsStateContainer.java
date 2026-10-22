/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.body.store;

import lombok.Getter;

/**
 * Stores previous physics states of bodies.
 * <p>
 * It is a single {@link PhysicsStateContainer} with the same buffer layout as the live container, just
 * {@code historySize} times larger. The state of body {@code i} that belongs to tick {@code t} lives at index
 * {@code capacity * (t % historySize) + i}, see {@link #indexOf(int, int)}.
 * <p>
 * The container does not decide <b>when</b> something is recorded, only <b>how</b>. The owner calls
 * {@link #write(int, PhysicsStateContainer)} right before the live state of a body gets overwritten:
 * <ul>
 *     <li>non-simulated client, {@code historySize == 1}: before applying a new network state, so the container
 *     holds the previous network state of each body;</li>
 *     <li>simulated worlds: {@code historySize == 0} for now, every call is a no-op. Reserved for prediction and rollback,
 *     where the size will be the number of ticks that can be rolled back.</li>
 * </ul>
 */
// todo: instead of creating a separate wrapper of PhysicsStateContainer, this could be implemented in a container itself, but I'm really not sure about this.
public class HistoricalPhysicsStateContainer {
    @Getter
    private final int historySize;

    /**
     * Backing container of {@code capacity * historySize} slots, {@code null} when the history is disabled.
     * <p>
     * Read body {@code i} of tick {@code t} at {@link #indexOf(int, int)}.
     */
    @Getter
    private PhysicsStateContainer data;

    /**
     * Number of body slots per tick.
     */
    @Getter
    private int capacity;

    /**
     * @param historySize number of states kept per body, {@code 0} disables the container
     * @param capacity    initial number of body slots per tick
     */
    public HistoricalPhysicsStateContainer(int historySize, int capacity) {
        if (historySize < 0) {
            throw new IllegalArgumentException("historySize cannot be negative: " + historySize);
        }
        this.historySize = historySize;
        this.capacity = capacity;
        this.data = historySize == 0 ? null : new PhysicsStateContainer(capacity * historySize);
    }

    public boolean isEnabled() {
        return historySize > 0;
    }

    /**
     * Returns the ring slot that stores the state of the given tick.
     */
    public int slotOf(int tick) {
        return Math.floorMod(tick, historySize);
    }

    /**
     * Returns the index in {@link #data} that stores the state of body {@code index} for the given tick.
     */
    public int indexOf(int index, int tick) {
        return capacity * slotOf(tick) + index;
    }

    /**
     * Copies the current state of {@code index} into the slot of the tick that state belongs to.
     * The tick is read from {@link PhysicsStateContainer#stateTick} of the live container.
     * Does nothing when the container is disabled.
     */
    public void write(int index, PhysicsStateContainer current) {
        if (historySize == 0) {
            return;
        }

        current.copyStateTo(index, data, indexOf(index, current.stateTick.get(index)));
    }

    /**
     * Grows the container, keeping the stored states. Every tick moves to its new position,
     * because the position of a tick depends on the capacity.
     */
    public void allocate(int newCapacity) {
        if (newCapacity == capacity) {
            return;
        }

        if (historySize > 0) {
            PhysicsStateContainer grown = new PhysicsStateContainer(newCapacity * historySize);
            int length = Math.min(capacity, newCapacity);

            for (int slot = 0; slot < historySize; slot++) {
                data.copyRangeTo(capacity * slot, grown, newCapacity * slot, length);
            }

            data = grown;
        }

        this.capacity = newCapacity;
    }

    /**
     * Clears the stored states of one body in every tick.
     */
    public void reset(int index) {
        for (int slot = 0; slot < historySize; slot++) {
            data.reset(capacity * slot + index);
        }
    }
}
