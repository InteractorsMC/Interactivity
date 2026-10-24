/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.world;

import lombok.Getter;
import lombok.Setter;
import org.clwn.interactivity.Interactivity;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

// todo: this might be a part of public API, but I'm not sure
public class SimulationPauseController {
    private static final long LONG_HOLD_NANOS = 1_000_000_000L;

    private final AtomicInteger holdCount = new AtomicInteger();

    @Getter
    @Setter
    private volatile boolean manuallyPaused;

    public boolean isPaused() {
        return manuallyPaused || holdCount.get() > 0;
    }

    public int getHoldCount() {
        return holdCount.get();
    }

    /**
     * Pauses the simulation until the returned hold is released.
     *
     * @param reason short description used in logs
     */
    public Hold acquire(String reason) {
        int count = holdCount.incrementAndGet();
        Interactivity.LOGGER.debug("Simulation pause acquired: {} (holds: {})", reason, count);
        return new Hold(reason);
    }

    public final class Hold {
        private final String reason;
        private final long acquiredAt = System.nanoTime();
        private final AtomicBoolean released = new AtomicBoolean();

        private Hold(String reason) {
            this.reason = reason;
        }

        /**
         * Releases this hold. Only the first call has an effect.
         */
        public void release() {
            if (!released.compareAndSet(false, true)) {
                return;
            }

            int count = holdCount.decrementAndGet();
            long heldNanos = System.nanoTime() - acquiredAt;

            if (heldNanos > LONG_HOLD_NANOS) {
                Interactivity.LOGGER.warn("Simulation was paused by {} for {} ms", reason, heldNanos / 1_000_000);
            }
            Interactivity.LOGGER.debug("Simulation pause released: {} after {} ms (holds: {})", reason, heldNanos / 1_000_000, count);
        }
    }
}
