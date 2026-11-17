package org.clwn.interactivity.core.event.external;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.api.event.*;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.clwn.interactivity.api.physics.event.PhysicsWorldEvent;
import org.clwn.interactivity.api.physics.event.body.BodyEvent;
import org.clwn.interactivity.api.physics.event.contact.ContactEvent;
import org.clwn.interactivity.core.InteractivityCore;
import org.clwn.interactivity.core.physics.world.PhysicsWorldImpl;
import org.jetbrains.annotations.Nullable;

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

    // @ContactNotifierEvent methods, every physics world creates a native contact notifier for each of them
    private final List<ContactListener> contactListeners = new ArrayList<>();

    public record ContactListener(
            Method method,
            @Nullable BodyType<?> bodyType,
            ContactNotifierEvent settings
    ) {
    }

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
            ContactNotifierEvent contactListener = method.getAnnotation(ContactNotifierEvent.class);
            if (contactListener == null && !method.isAnnotationPresent(SubscribeEvent.class)) {
                continue;
            }

            validateListener(method, contactListener != null);

            if (InteractivityCore.isRegistrationPhaseDone() && RegistrationEvent.class.isAssignableFrom(method.getParameterTypes()[0])) {
                throw new IllegalStateException(
                        "Registration events were already fired, register this listener during mod initialization: " + method
                );
            }

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

            if (contactListener != null) {
                contactListeners.add(new ContactListener(method, null, contactListener));
                continue;
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
            ContactNotifierEvent contactListener = method.getAnnotation(ContactNotifierEvent.class);
            if (contactListener == null && !method.isAnnotationPresent(SubscribeEvent.class)) {
                continue;
            }

            validateListener(method, contactListener != null);

            if (!Modifier.isPublic(method.getModifiers())) {
                throw new IllegalArgumentException(
                        "Global event handler must be public: " + method
                );
            }

            if (contactListener != null) {
                if (!contactListener.bodyType1().isEmpty()) {
                    throw new IllegalArgumentException(
                            "@ContactNotifierEvent method of a body class receives the contacts of its body, bodyType can't be set: " + method
                    );
                }
                contactListeners.add(new ContactListener(method, bodyType, contactListener));
                continue;
            }

            listeners.add(new RegisteredListener(method.getParameterTypes()[0], method, listensCustomEvents));
        }

        bodyTypeEventListeners.put(bodyType, listeners);
    }

    private static void validateListener(Method method, boolean isContactListener) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Class<?> eventType = isContactListener ? ContactEvent.class : Event.class;
        String annotation = isContactListener ? "@ContactNotifierEvent" : "@SubscribeEvent";

        if (parameterTypes.length != 1
                || !eventType.isAssignableFrom(parameterTypes[0])) {
            throw new IllegalArgumentException(
                    annotation + " method must accept exactly one " + eventType.getSimpleName()
                            + " subtype: " + method
            );
        }

        if (method.getReturnType() != void.class) {
            throw new IllegalArgumentException(
                    annotation + " method must return void: " + method
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

    public List<ContactListener> getContactListeners() {
        return List.copyOf(contactListeners);
    }

    public void publishContactEvent(ContactListener listener, ContactEvent event) {
        invokeListener(listener.method(), listener.bodyType() != null ? event.getBody() : null, event);
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
