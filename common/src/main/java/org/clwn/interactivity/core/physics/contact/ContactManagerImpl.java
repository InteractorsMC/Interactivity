/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.contact;

import com.github.stephengold.joltjni.Jolt;
import com.github.stephengold.joltjni.RVec3;
import com.github.stephengold.joltjni.Vec3;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.api.event.ContactNotifierEvent;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.clwn.interactivity.api.physics.contact.ContactManager;
import org.clwn.interactivity.api.physics.contact.ContactNotifier;
import org.clwn.interactivity.api.physics.event.contact.*;
import org.clwn.interactivity.core.event.external.EventBusImpl;
import org.clwn.interactivity.core.physics.body.SimulatedBodyManager;
import org.clwn.interactivity.core.physics.body.type.BodyTypeImpl;
import org.clwn.interactivity.core.physics.body.type.BodyTypeRegistryImpl;
import org.clwn.interactivity.core.physics.world.SimulatedPhysicsWorldImpl;
import org.clwn.interactivity.natives.ContactNative;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

// todo: this code is reviewed, but not cleaned up properly
public class ContactManagerImpl implements ContactManager {
    private final SimulatedPhysicsWorldImpl world;

    private List<EventBusImpl.ContactListener> listeners = List.of();
    private final Int2ObjectMap<RuntimeNotifier> notifiers = new Int2ObjectOpenHashMap<>();
    private int nextNotifierId;
    private long handle;

    private record RuntimeNotifier(ContactNotifier settings, Consumer<ContactEvent> callback) {
    }

    public ContactManagerImpl(SimulatedPhysicsWorldImpl world) {
        this.world = world;
    }

    public void init() {
        handle = ContactNative.create(world.getJoltBridge().getPhysicsSystem().va());

        listeners = EventBusImpl.INSTANCE.getContactListeners();
        for (int id = 0; id < listeners.size(); id++) {
            addNotifier(id, listeners.get(id));
        }
        nextNotifierId = listeners.size();
    }

    public void close() {
        if (handle != 0) {
            ContactNative.destroy(handle);
            handle = 0;
            notifiers.clear();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends ContactEvent> ContactNotifier addNotifier(Class<E> eventType, Consumer<ContactNotifier> configurator, Consumer<E> callback) {
        ContactNotifier notifier = new ContactNotifier();
        configurator.accept(notifier);
        world.execute(() -> addNotifier(new RuntimeNotifier(notifier, (Consumer<ContactEvent>) callback), kindsOf(eventType)));
        return notifier;
    }

    @Override
    public void removeNotifier(ContactNotifier notifier) {
        world.execute(() -> notifiers.int2ObjectEntrySet().removeIf(entry -> {
            if (handle == 0 || entry.getValue().settings() != notifier) {
                return false;
            }
            ContactNative.removeNotifier(handle, entry.getIntKey());
            return true;
        }));
    }

    @Override
    public void ignoreCollision(Body body1, Body body2) {
        world.execute(() -> setIgnored(body1, body2, true));
    }

    @Override
    public void unignoreCollision(Body body1, Body body2) {
        world.execute(() -> setIgnored(body1, body2, false));
    }

    /**
     * Called right before the Jolt body is destroyed.
     */
    public void onBodyRemoving(UUID body) {
        int bodyId = world.getBodyManager().getJoltBodyId(body);
        if (handle != 0 && bodyId != Jolt.cInvalidBodyId) {
            ContactNative.removeBody(handle, bodyId);
        }
        notifiers.values().removeIf(notifier -> isBoundTo(notifier.settings(), body));
    }

    /**
     * Publishes the contacts of the last physics update.
     */
    public void drain() {
        ByteBuffer events = handle == 0 ? null : ContactNative.getEvents(handle);
        if (events == null) {
            return;
        }

        events.order(ByteOrder.nativeOrder());
        try {
            for (int offset = 0; offset < events.capacity(); offset += ContactNative.RECORD_SIZE) {
                publish(events, offset);
            }
        } finally {
            ContactNative.clearEvents(handle);
        }
    }

    private void publish(ByteBuffer events, int offset) {
        SimulatedBodyManager bodyManager = world.getBodyManager();

        Body body = bodyManager.getBodyByJoltId(events.getInt(offset + ContactNative.RECORD_BODY_1));
        if (body == null) {
            return;
        }

        boolean terrain = events.getInt(offset + ContactNative.RECORD_TYPE_2) == ContactNative.BODY_TYPE_TERRAIN;
        Body other = terrain ? null : bodyManager.getBodyByJoltId(events.getInt(offset + ContactNative.RECORD_BODY_2));

        int kind = events.getInt(offset + ContactNative.RECORD_KIND);
        ContactEvent event;
        if (kind == ContactNative.KIND_REMOVED) {
            event = new ContactRemovedEvent(world, body, other, terrain);
        } else {
            int point = offset + ContactNative.RECORD_POINT;
            int normal = offset + ContactNative.RECORD_NORMAL;
            RVec3 contactPoint = new RVec3(events.getDouble(point), events.getDouble(point + 8), events.getDouble(point + 16));
            Vec3 contactNormal = new Vec3(events.getFloat(normal), events.getFloat(normal + 4), events.getFloat(normal + 8));
            float penetration = events.getFloat(offset + ContactNative.RECORD_PENETRATION);
            float approachSpeed = events.getFloat(offset + ContactNative.RECORD_APPROACH_SPEED);
            int otherSubShapeUserData = events.getInt(offset + ContactNative.RECORD_SUB_SHAPE_USER_DATA_2);

            event = kind == ContactNative.KIND_ADDED
                    ? new ContactAddedEvent(world, body, other, terrain, contactPoint, contactNormal, penetration, approachSpeed, otherSubShapeUserData)
                    : new ContactPersistedEvent(world, body, other, terrain, contactPoint, contactNormal, penetration, approachSpeed, otherSubShapeUserData);
        }

        int notifierId = events.getInt(offset + ContactNative.RECORD_NOTIFIER_ID);
        if (notifierId < listeners.size()) {
            EventBusImpl.INSTANCE.publishContactEvent(listeners.get(notifierId), event);
            return;
        }

        RuntimeNotifier notifier = notifiers.get(notifierId);
        if (notifier == null) {
            return;
        }
        try {
            notifier.callback().accept(event);
        } catch (RuntimeException e) {
            Interactivity.LOGGER.error("Contact notifier failed to handle {}", event.getClass().getSimpleName(), e);
        }
    }

    private void setIgnored(Body body1, Body body2, boolean ignored) {
        int id1 = joltIdOf(body1);
        int id2 = joltIdOf(body2);
        if (handle == 0 || id1 == Jolt.cInvalidBodyId || id2 == Jolt.cInvalidBodyId) {
            return;
        }

        if (ignored) {
            ContactNative.ignorePair(handle, id1, id2);
        } else {
            ContactNative.unignorePair(handle, id1, id2);
        }
    }

    private void addNotifier(int id, EventBusImpl.ContactListener listener) {
        ContactNotifierEvent settings = listener.settings();
        try {
            int type1 = listener.bodyType() != null ? typeId(listener.bodyType()) : typeId(settings.bodyType1());
            int type2 = settings.terrainOnly() ? ContactNative.BODY_TYPE_TERRAIN : typeId(settings.bodyType2());
            ContactNative.addNotifier(handle, id, kindsOf(listener.method().getParameterTypes()[0]),
                    ContactNative.ANY, type1, ContactNative.ANY, type2, settings.minApproachSpeed());
        } catch (IllegalArgumentException e) {
            Interactivity.LOGGER.warn("@ContactNotifierEvent {} is skipped: {}", listener.method(), e.getMessage());
        }
    }

    private void addNotifier(RuntimeNotifier notifier, int kinds) {
        ContactNotifier settings = notifier.settings();
        if (handle == 0 || isMissing(settings.getBody1()) || isMissing(settings.getBody2())) {
            return;
        }

        int id = nextNotifierId++;
        int body1 = settings.getBody1() != null ? joltIdOf(settings.getBody1()) : ContactNative.ANY;
        int body2 = settings.getBody2() != null ? joltIdOf(settings.getBody2()) : ContactNative.ANY;
        int type1 = settings.getBodyType1() != null ? typeId(settings.getBodyType1()) : ContactNative.ANY;
        int type2 = settings.isTerrainOnly() ? ContactNative.BODY_TYPE_TERRAIN
                : settings.getBodyType2() != null ? typeId(settings.getBodyType2()) : ContactNative.ANY;
        ContactNative.addNotifier(handle, id, kinds, body1, type1, body2, type2, settings.getMinApproachSpeed());
        notifiers.put(id, notifier);
    }

    private int joltIdOf(Body body) {
        return world.getBodyManager().getJoltBodyId(body.getUuid());
    }

    /**
     * A body filter without a Jolt body can never match, {@link Jolt#cInvalidBodyId} is {@link ContactNative#ANY}
     */
    private boolean isMissing(@Nullable Body body) {
        return body != null && joltIdOf(body) == Jolt.cInvalidBodyId;
    }

    private static boolean isBoundTo(ContactNotifier notifier, UUID body) {
        return (notifier.getBody1() != null && notifier.getBody1().getUuid().equals(body))
                || (notifier.getBody2() != null && notifier.getBody2().getUuid().equals(body));
    }

    private static int typeId(String id) {
        if (id.isEmpty()) {
            return ContactNative.ANY;
        }

        ResourceLocation location = ResourceLocation.tryParse(id);
        BodyType<?> type = location == null ? null : BodyTypeRegistryImpl.INSTANCE.get(location).orElse(null);
        if (type == null) {
            throw new IllegalArgumentException("unknown body type " + id);
        }
        return typeId(type);
    }

    private static int typeId(BodyType<?> type) {
        return ((BodyTypeImpl<?>) type).getNumericId();
    }

    private static int kindsOf(Class<?> eventType) {
        if (eventType == ContactAddedEvent.class) {
            return ContactNative.KIND_ADDED;
        }
        if (eventType == ContactPersistedEvent.class) {
            return ContactNative.KIND_PERSISTED;
        }
        if (eventType == ContactRemovedEvent.class) {
            return ContactNative.KIND_REMOVED;
        }
        if (eventType == ContactPointEvent.class) {
            return ContactNative.KIND_ADDED | ContactNative.KIND_PERSISTED;
        }
        return ContactNative.KIND_ADDED | ContactNative.KIND_PERSISTED | ContactNative.KIND_REMOVED;
    }
}
