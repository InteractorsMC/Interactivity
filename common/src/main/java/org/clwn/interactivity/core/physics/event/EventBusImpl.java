package org.clwn.interactivity.core.physics.event;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.clwn.interactivity.api.physics.event.BodyEvent;
import org.clwn.interactivity.api.physics.event.Event;
import org.clwn.interactivity.api.physics.event.EventBus;
import org.clwn.interactivity.api.physics.event.SubscribeEvent;
import org.clwn.interactivity.core.physics.world.SimulatedPhysicsWorldImpl;

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
            Method method
    ) {
    }

    private EventBusImpl() {
    }

    @Override
    public void registerListener(Class<?> clazz) {
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
                    method
            ));
        }
    }

    public <T extends Body> void registerBodyType(BodyType<T> bodyType) {
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

            listeners.add(new RegisteredListener(method.getParameterTypes()[0], method));
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
            if (!listener.eventType().isAssignableFrom(event.getClass())) {
                continue;
            }

            invokeListener(listener.method(), null, event);
        }

        // todo: instanceof PhysicsEvent
        if (event instanceof BodyEvent bodyEvent) {
            publishToBody(bodyEvent.getBody(), event);
            return;
        }

        if (event.getPhysicsWorld() instanceof SimulatedPhysicsWorldImpl physicsWorld) {
            for (Body body : physicsWorld.getBodyManager().getBodyDataStore().getPhysicsStateContainer().bodies) {
                if (body == null) {
                    continue;
                }

                publishToBody(body, event);
            }
        }
    }

    private void publishToBody(
            Body body,
            Event event
    ) {
        List<RegisteredListener> listeners =
                bodyTypeEventListeners.get(body.getType());

        if (listeners == null) {
            return;
        }

        for (RegisteredListener listener : listeners) {
            if (!listener.eventType().isAssignableFrom(event.getClass())) {
                continue;
            }

            invokeListener(listener.method(), body, event);
        }
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
