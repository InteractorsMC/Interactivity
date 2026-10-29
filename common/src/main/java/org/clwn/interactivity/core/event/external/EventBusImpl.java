package org.clwn.interactivity.core.event.external;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.api.event.CustomEvent;
import org.clwn.interactivity.api.event.Event;
import org.clwn.interactivity.api.event.EventBus;
import org.clwn.interactivity.api.event.SubscribeEvent;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.clwn.interactivity.api.physics.event.BodyEvent;
import org.clwn.interactivity.api.physics.event.PhysicsWorldEvent;
import org.clwn.interactivity.core.physics.world.PhysicsWorldImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public final class EventBusImpl implements EventBus {
    public static final EventBusImpl INSTANCE = new EventBusImpl();

    // Static listeners for global events.
    private final List<RegisteredListener> eventListeners = new ArrayList<>();

    // Every body type is mapped to a single Body class, which have same listeners
    private final Object2ObjectArrayMap<BodyType<?>, List<RegisteredListener>> bodyTypeEventListeners = new Object2ObjectArrayMap<>();

    private record RegisteredListener(
            Class<?> eventType,
            Method method,
            boolean listensCustomEvents
    ) {
    }

    private EventBusImpl() {
    }

    @Override
    public void registerListener(Class<?> clazz, boolean listensCustomEvents) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(SubscribeEvent.class)) {
                continue;
            }

            validateListener(method);

            if (!Modifier.isPublic(method.getModifiers())) {
                throw new IllegalArgumentException(
                        "Global event handler must be public: " + method
                );
            }

            if (!Modifier.isStatic(method.getModifiers())) {
                throw new IllegalArgumentException(
                        "Global event handler must be static: " + method
                );
            }

            eventListeners.add(new RegisteredListener(
                    method.getParameterTypes()[0],
                    method,
                    listensCustomEvents
            ));
        }
    }

    public <T extends Body> void registerBodyType(BodyType<T> bodyType, boolean listensCustomEvents) {
        List<RegisteredListener> listeners = new ArrayList<>();
        for (Method method : bodyType.getBodyClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(SubscribeEvent.class)) {
                continue;
            }

            validateListener(method);

            if (!Modifier.isPublic(method.getModifiers())) {
                throw new IllegalArgumentException(
                        "Global event handler must be public: " + method
                );
            }

            listeners.add(new RegisteredListener(method.getParameterTypes()[0], method, listensCustomEvents));
        }

        bodyTypeEventListeners.put(bodyType, listeners);
    }

    private static void validateListener(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();

        if (parameterTypes.length != 1
                || !Event.class.isAssignableFrom(parameterTypes[0])) {
            throw new IllegalArgumentException(
                    "@SubscribeEvent method must accept exactly one Event "
                            + "subtype: " + method
            );
        }

        if (method.getReturnType() != void.class) {
            throw new IllegalArgumentException(
                    "@SubscribeEvent method must return void: " + method
            );
        }
    }

    public void publishEvent(Event event) {
        // todo: applicable listeners can be cached using Event -> listeners

        for (RegisteredListener listener : eventListeners) {
            if (shouldSkipEvent(event, listener)) {
                continue;
            }

            invokeListener(listener.method(), null, event);
        }

        if (event instanceof PhysicsWorldEvent physicsWorldEvent) {
            if (event instanceof BodyEvent bodyEvent) {
                publishToBody(bodyEvent.getBody(), physicsWorldEvent);
                return;
            }

            if (physicsWorldEvent.getPhysicsWorld() instanceof PhysicsWorldImpl physicsWorld) {
                for (Body body : physicsWorld.getBodyManager().getBodyDataStore().getPhysicsStateContainer().bodies) {
                    if (body == null) {
                        continue;
                    }

                    publishToBody(body, physicsWorldEvent);
                }
            }
        }
    }

    private void publishToBody(
            Body body,
            PhysicsWorldEvent event
    ) {
        List<RegisteredListener> listeners =
                bodyTypeEventListeners.get(body.getType());

        if (listeners == null) {
            return;
        }

        for (RegisteredListener listener : listeners) {
            if (shouldSkipEvent(event, listener)) {
                continue;
            }

            invokeListener(listener.method(), body, event);
        }
    }

    private static boolean shouldSkipEvent(Event event, RegisteredListener listener) {
        return
                !listener.eventType().isAssignableFrom(event.getClass())
                        || (!listener.listensCustomEvents && event instanceof CustomEvent);
    }

    private static void invokeListener(Method method, Object target, Event event) {
        try {
            method.invoke(target, event);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;

            Interactivity.LOGGER.error(
                    "Listener {}#{} failed to handle event {}",
                    target != null
                            ? target.getClass().getSimpleName()
                            : method.getDeclaringClass().getSimpleName(),
                    method.getName(),
                    event.getClass().getSimpleName(),
                    cause
            );
        } catch (Exception e) {
            Interactivity.LOGGER.error(
                    "Failed to invoke listener {}#{} for event {}",
                    target.getClass().getSimpleName(),
                    method.getName(),
                    event.getClass().getSimpleName(),
                    e
            );
        }
    }
}
