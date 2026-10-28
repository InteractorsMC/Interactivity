/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.constraint;

import com.github.stephengold.joltjni.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.clwn.interactivity.Interactivity;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.constraint.Constraint;
import org.clwn.interactivity.api.physics.constraint.ConstraintManager;
import org.clwn.interactivity.api.physics.constraint.ConstraintType;
import org.clwn.interactivity.core.physics.world.SimulatedPhysicsWorldImpl;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class ConstraintManagerImpl implements ConstraintManager {
    private final SimulatedPhysicsWorldImpl world;

    private final Object2ObjectOpenHashMap<UUID, ConstraintImpl<?>> constraints = new Object2ObjectOpenHashMap<>();

    private final Object2ObjectOpenHashMap<UUID, ObjectArrayList<ConstraintImpl<?>>> byBody = new Object2ObjectOpenHashMap<>();

    public ConstraintManagerImpl(SimulatedPhysicsWorldImpl world) {
        this.world = world;
    }

    @Override
    public <S extends TwoBodyConstraintSettings, J extends TwoBodyConstraint> Constraint<J> createConstraint(
            Body body1, @Nullable Body body2, ConstraintType<S, J> type,
            Consumer<S> settings, Consumer<J> configurator
    ) {
        ConstraintImpl<J> constraint = new ConstraintImpl<>(
                this, UUID.randomUUID(), type, body1.getUuid(), body2 == null ? null : body2.getUuid()
        );
        world.execute(() -> activate(constraint, type, settings, configurator));
        return constraint;
    }

    public @Nullable ConstraintImpl<?> restoreConstraint(UUID uuid, UUID body1, @Nullable UUID body2, TwoBodyConstraintSettings settings, byte[] state) {
        TwoBodyConstraint created;
        try {
            created = createJoltConstraint(uuid, body1, body2, settings);
        } catch (Exception e) {
            Interactivity.LOGGER.error("Failed to restore constraint {}", uuid, e);
            return null;
        }
        if (created == null) {
            return null;
        }

        TwoBodyConstraintRef ref = created.toRef();
        ConstraintType<?, ?> type = ConstraintType.bySubType(created.getSubType());
        if (type == null) {
            Interactivity.LOGGER.warn("Constraint {} was not restored: unsupported type {}", uuid, created.getSubType());
            ref.close();
            return null;
        }

        if (state.length > 0) {
            try (StateRecorderImpl recorder = new StateRecorderImpl()) {
                recorder.writeBytes(state);
                created.restoreState(recorder);
            } catch (Exception e) {
                Interactivity.LOGGER.warn("Failed to restore the state of constraint {}, using defaults", uuid, e);
            }
        }

        ConstraintImpl<?> constraint = new ConstraintImpl<>(this, uuid, type, body1, body2);
        addToSimulation(constraint, ref, created);
        return constraint;
    }

    @Override
    public void removeConstraint(Constraint<?> constraint) {
        world.execute(() -> removeInternal((ConstraintImpl<?>) constraint));
    }

    @Override
    public @Nullable Constraint<?> getConstraint(UUID id) {
        return constraints.get(id);
    }

    @Override
    public void forEachConstraint(Body body, Consumer<Constraint<?>> action) {
        ObjectArrayList<ConstraintImpl<?>> list = byBody.get(body.getUuid());
        if (list != null) {
            list.forEach(action);
        }
    }

    public int getConstraintCount() {
        return constraints.size();
    }

    <J extends TwoBodyConstraint> void modify(ConstraintImpl<J> constraint, Consumer<J> action) {
        world.execute(() -> {
            if (constraint.state != Constraint.State.ACTIVE) {
                return;
            }

            try {
                action.accept(constraint.joltConstraint);
            } catch (Exception e) {
                Interactivity.LOGGER.error("Failed to modify constraint {}", constraint.getUuid(), e);
            }
            world.getJoltBridge().getBodyInterface().activateConstraint(constraint.joltConstraint);
        });
    }

    /**
     * Removes all constraints of a body. Called right before the Jolt body is destroyed.
     */
    public void onBodyRemoving(UUID body) {
        ObjectArrayList<ConstraintImpl<?>> list = byBody.get(body);
        if (list == null) {
            return;
        }

        // removeInternal unlinks from the list, so iterate over a copy
        for (ConstraintImpl<?> constraint : list.toArray(new ConstraintImpl<?>[0])) {
            removeInternal(constraint);
        }
    }

    /**
     * Collects the bodies connected to {@code origin} through constraints, including {@code origin} itself,
     * and the constraints between them.
     */
    public void collectGroup(UUID origin, Set<UUID> outBodies, List<ConstraintImpl<?>> outConstraints) {
        ObjectArrayFIFOQueue<UUID> queue = new ObjectArrayFIFOQueue<>();
        ReferenceOpenHashSet<ConstraintImpl<?>> viewed = new ReferenceOpenHashSet<>();
        if (outBodies.add(origin)) {
            queue.enqueue(origin);
        }

        while (!queue.isEmpty()) {
            ObjectArrayList<ConstraintImpl<?>> list = byBody.get(queue.dequeue());
            if (list == null) {
                continue;
            }

            for (ConstraintImpl<?> constraint : list) {
                if (!viewed.add(constraint)) {
                    continue;
                }
                outConstraints.add(constraint);

                if (outBodies.add(constraint.getBody1())) {
                    queue.enqueue(constraint.getBody1());
                }
                UUID body2 = constraint.getBody2();
                if (body2 != null && outBodies.add(body2)) {
                    queue.enqueue(body2);
                }
            }
        }
    }

    public void close() {
        PhysicsSystem physicsSystem = world.getJoltBridge().getPhysicsSystem();
        for (ConstraintImpl<?> constraint : constraints.values()) {
            physicsSystem.removeConstraint(constraint.joltConstraint);
            constraint.joltRef.close();
            constraint.joltRef = null;
            constraint.joltConstraint = null;
            constraint.state = Constraint.State.REMOVED;
        }
        constraints.clear();
        byBody.clear();
    }

    private <S extends TwoBodyConstraintSettings, J extends TwoBodyConstraint> void activate(
            ConstraintImpl<J> constraint, ConstraintType<S, J> type,
            Consumer<S> settingsConsumer, Consumer<J> configurator
    ) {
        TwoBodyConstraint created;
        try (S settings = type.createSettings()) {
            settingsConsumer.accept(settings);
            created = createJoltConstraint(constraint.getUuid(), constraint.getBody1(), constraint.getBody2(), settings);
        } catch (Exception e) {
            Interactivity.LOGGER.error("Failed to create constraint {}", constraint.getUuid(), e);
            constraint.state = Constraint.State.REMOVED;
            return;
        }
        if (created == null) {
            constraint.state = Constraint.State.REMOVED;
            return;
        }

        TwoBodyConstraintRef ref = created.toRef();

        try {
            J joltConstraint = (J) created;
            configurator.accept(joltConstraint);
        } catch (Exception e) {
            Interactivity.LOGGER.error("Failed to configure constraint {}", constraint.getUuid(), e);
            ref.close();
            constraint.state = Constraint.State.REMOVED;
            return;
        }

        addToSimulation(constraint, ref, created);
    }

    /**
     * Creates the Jolt constraint between two bodies, or between a body and the world if {@code body2} is {@code null}.
     *
     * @return the constraint (not referenced yet), or {@code null} if a body does not exist
     */
    private @Nullable TwoBodyConstraint createJoltConstraint(UUID uuid, UUID body1, @Nullable UUID body2, TwoBodyConstraintSettings settings) {
        int bodyId1 = world.getBodyManager().getJoltBodyId(body1);
        int bodyId2 = body2 == null ? Jolt.cInvalidBodyId : world.getBodyManager().getJoltBodyId(body2);
        if (bodyId1 == Jolt.cInvalidBodyId || (body2 != null && bodyId2 == Jolt.cInvalidBodyId)) {
            Interactivity.LOGGER.warn("Constraint {} was not created: body {} or {} does not exist", uuid, body1, body2);
            return null;
        }
        if (bodyId1 == bodyId2) {
            Interactivity.LOGGER.warn("Constraint {} was not created: both ends are body {}", uuid, body1);
            return null;
        }

        return world.getJoltBridge().getBodyInterface().createConstraint(settings, bodyId1, bodyId2);
    }

    private <J extends TwoBodyConstraint> void addToSimulation(ConstraintImpl<J> constraint, TwoBodyConstraintRef ref, TwoBodyConstraint created) {
        J joltConstraint = (J) created;
        constraint.joltRef = ref;
        constraint.joltConstraint = joltConstraint;

        world.getJoltBridge().getPhysicsSystem().addConstraint(joltConstraint);
        world.getJoltBridge().getBodyInterface().activateConstraint(joltConstraint);

        constraints.put(constraint.getUuid(), constraint);
        link(constraint.getBody1(), constraint);
        if (constraint.getBody2() != null) {
            link(constraint.getBody2(), constraint);
        }
        constraint.state = Constraint.State.ACTIVE;
    }

    private void removeInternal(ConstraintImpl<?> constraint) {
        if (constraint.state != Constraint.State.ACTIVE) {
            constraint.state = Constraint.State.REMOVED;
            return;
        }

        world.getJoltBridge().getBodyInterface().activateConstraint(constraint.joltConstraint);
        world.getJoltBridge().getPhysicsSystem().removeConstraint(constraint.joltConstraint);
        constraint.joltRef.close();
        constraint.joltRef = null;
        constraint.joltConstraint = null;

        constraints.remove(constraint.getUuid());
        unlink(constraint.getBody1(), constraint);
        if (constraint.getBody2() != null) {
            unlink(constraint.getBody2(), constraint);
        }
        constraint.state = Constraint.State.REMOVED;
    }

    private void link(UUID body, ConstraintImpl<?> constraint) {
        byBody.computeIfAbsent(body, k -> new ObjectArrayList<>()).add(constraint);
    }

    private void unlink(UUID body, ConstraintImpl<?> constraint) {
        ObjectArrayList<ConstraintImpl<?>> list = byBody.get(body);
        if (list != null && list.remove(constraint) && list.isEmpty()) {
            byBody.remove(body);
        }
    }
}

