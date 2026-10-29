/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.event.internal;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class Event<T> {

    private final List<Consumer<T>> listeners = new CopyOnWriteArrayList<>();

    public Subscription subscribe(Consumer<T> listener) {
        listeners.add(listener);

        AtomicBoolean closed = new AtomicBoolean();
        return () -> {
            if (closed.compareAndSet(false, true)) {
                unsubscribe(listener);
            }
        };
    }

    public void unsubscribe(Consumer<T> listener) {
        listeners.remove(listener);
    }

    public void publish(T event) {
        for (Consumer<T> listener : listeners) {
            listener.accept(event);
        }
    }

    @FunctionalInterface
    public interface Subscription extends AutoCloseable {
        @Override
        void close();
    }
}