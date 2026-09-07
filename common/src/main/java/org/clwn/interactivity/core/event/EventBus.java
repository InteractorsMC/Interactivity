/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class EventBus {

    private static final Map<Class<?>, List<Consumer<?>>> listeners =
            new ConcurrentHashMap<>();

    private EventBus() {}

    public static <T> void subscribe(
            Class<T> eventType,
            Consumer<T> listener
    ) {
        listeners
                .computeIfAbsent(eventType, clazz -> new CopyOnWriteArrayList<>())
                .add(listener);
    }

    public static <T> void publish(T event) {
        var eventType = event.getClass();

        var eventListeners = listeners.get(eventType);
        if (eventListeners == null) {
            return;
        }

        for (Consumer<?> listener : eventListeners) {
            @SuppressWarnings("unchecked")
            Consumer<T> typedListener = (Consumer<T>) listener;

            typedListener.accept(event);
        }
    }
}