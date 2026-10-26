/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.command;

import com.github.stephengold.joltjni.Quat;
import com.github.stephengold.joltjni.RVec3;
import com.github.stephengold.joltjni.enumerate.EConstraintSpace;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.clwn.interactivity.api.physics.constraint.Constraint;
import org.clwn.interactivity.api.physics.constraint.ConstraintManager;
import org.clwn.interactivity.api.physics.constraint.ConstraintType;
import org.clwn.interactivity.core.physics.command.argument.BodySelector;
import org.clwn.interactivity.core.physics.command.argument.BodySelectorArgument;
import org.clwn.interactivity.core.physics.constraint.ConstraintImpl;
import org.clwn.interactivity.core.physics.constraint.ConstraintManagerImpl;
import org.clwn.interactivity.core.physics.world.ServerPhysicsWorldImpl;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class ConstraintSubCommand {
    private static final int MAX_CHAIN_LENGTH = 64;
    private static final int DEFAULT_CHAIN_LENGTH = 8;

    private static final double CHAIN_SPACING = 1.25;

    private ConstraintSubCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal("constraint")
                .then(getChainCommand())
                .then(Commands.literal("remove")
                        .then(Commands.argument("bodies", BodySelectorArgument.bodies())
                                .executes(ConstraintSubCommand::remove)
                        )
                )
                .then(Commands.literal("info")
                        .executes(context -> info(context, null))
                        .then(Commands.argument("bodies", BodySelectorArgument.bodies())
                                .executes(context -> info(context, BodySelectorArgument.getBodies(context, "bodies")))
                        )
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> getChainCommand() {
        LiteralArgumentBuilder<CommandSourceStack> chain = Commands.literal("chain");
        for (ChainLink link : ChainLink.values()) {
            chain.then(Commands.literal(link.getName())
                    .then(Commands.argument("type", ResourceLocationArgument.id())
                            .suggests(SummonSubCommand.SUMMONABLE_TYPES)
                            .executes(context -> chain(context, link, context.getSource().getPosition(), DEFAULT_CHAIN_LENGTH))
                            .then(Commands.argument("pos", Vec3Argument.vec3())
                                    .executes(context -> chain(context, link, Vec3Argument.getVec3(context, "pos"), DEFAULT_CHAIN_LENGTH))
                                    .then(Commands.argument("count", IntegerArgumentType.integer(1, MAX_CHAIN_LENGTH))
                                            .executes(context -> chain(
                                                    context,
                                                    link,
                                                    Vec3Argument.getVec3(context, "pos"),
                                                    IntegerArgumentType.getInteger(context, "count")
                                            ))
                                    )
                            )
                    )
            );
        }
        return chain;
    }

    private static int chain(CommandContext<CommandSourceStack> context, ChainLink link, Vec3 position, int count) {
        CommandSourceStack source = context.getSource();

        BodyType<? extends Body> type = SummonSubCommand.getSummonableType(source, ResourceLocationArgument.getId(context, "type"));
        if (type == null) {
            return 0;
        }

        ServerPhysicsWorldImpl world = InteractivityCommand.getPhysicsWorld(source);
        if (world == null) {
            return 0;
        }

        spawnChain(world, type, link, position, count);

        source.sendSuccess(() -> Component.literal("Created a " + link.getName() + " chain of " + count + " " + type.getTypeId()), true);
        return count;
    }

    private static <T extends Body> void spawnChain(ServerPhysicsWorldImpl world, BodyType<T> type, ChainLink link, Vec3 position, int count) {
        ConstraintManager constraintManager = world.getConstraintManager();

        Body previous = null;
        for (int i = 0; i < count; i++) {
            RVec3 centre = new RVec3(position.x + i * CHAIN_SPACING, position.y, position.z);
            RVec3 previousCentre = new RVec3(centre.xx() - CHAIN_SPACING, centre.yy(), centre.zz());

            T body = world.getBodyManager().spawnBody(type, centre, Quat.sIdentity(), b -> {});
            if (previous == null) {
                link.create(constraintManager, body, null, centre, previousCentre);
            } else {
                link.create(constraintManager, previous, body, previousCentre, centre);
            }
            previous = body;
        }
    }

    private static int remove(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPhysicsWorldImpl world = InteractivityCommand.getPhysicsWorld(source);
        if (world == null) {
            return 0;
        }

        BodySelector selector = BodySelectorArgument.getBodies(context, "bodies");
        Vec3 origin = source.getPosition();

        world.execute(() -> {
            ConstraintManagerImpl constraintManager = world.getConstraintManager();

            ReferenceOpenHashSet<Constraint<?>> constraints = new ReferenceOpenHashSet<>();
            for (Body body : selector.select(world, origin)) {
                constraintManager.forEachConstraint(body, constraints::add);
            }
            constraints.forEach(constraintManager::removeConstraint);

            if (constraints.isEmpty()) {
                source.sendFailure(Component.literal("No constraints were found"));
            } else {
                source.sendSuccess(() -> Component.literal("Removed " + constraints.size() + " constraints"), true);
            }
        });
        return 1;
    }

    private static int info(CommandContext<CommandSourceStack> context, @Nullable BodySelector selector) {
        CommandSourceStack source = context.getSource();
        ServerPhysicsWorldImpl world = InteractivityCommand.getPhysicsWorld(source);
        if (world == null) {
            return 0;
        }

        Vec3 origin = source.getPosition();

        world.execute(() -> {
            ConstraintManagerImpl constraintManager = world.getConstraintManager();
            if (selector == null) {
                source.sendSuccess(() -> Component.literal("Constraints in this dimension: " + constraintManager.getConstraintCount()), true);
                return;
            }

            List<Body> bodies = selector.select(world, origin);
            if (bodies.isEmpty()) {
                source.sendFailure(Component.literal("No bodies were found"));
                return;
            }

            Set<UUID> visited = new ObjectOpenHashSet<>();
            IntArrayList groupSizes = new IntArrayList();
            int constraintCount = 0;
            for (Body body : bodies) {
                if (visited.contains(body.getUuid())) {
                    continue;
                }

                Set<UUID> groupBodies = new ObjectOpenHashSet<>();
                List<ConstraintImpl<?>> groupConstraints = new ObjectArrayList<>();
                constraintManager.collectGroup(body.getUuid(), groupBodies, groupConstraints);

                visited.addAll(groupBodies);
                groupSizes.add(groupBodies.size());
                constraintCount += groupConstraints.size();
            }

            int finalConstraintCount = constraintCount;
            source.sendSuccess(() -> Component.literal(bodies.size() + " bodies in " + groupSizes.size()
                    + " groups with " + finalConstraintCount + " constraints, group sizes: " + groupSizes), true);
        });
        return 1;
    }

    private enum ChainLink {
        FIXED {
            @Override
            void create(ConstraintManager manager, Body body1, @Nullable Body body2, RVec3 centre1, RVec3 centre2) {
                RVec3 pivot = midpoint(centre1, centre2);
                manager.createConstraint(body1, body2, ConstraintType.FIXED, settings -> {
                    settings.setSpace(EConstraintSpace.WorldSpace);
                    settings.setPoint1(pivot);
                    settings.setPoint2(pivot);
                });
            }
        },
        POINT {
            @Override
            void create(ConstraintManager manager, Body body1, @Nullable Body body2, RVec3 centre1, RVec3 centre2) {
                RVec3 pivot = midpoint(centre1, centre2);
                manager.createConstraint(body1, body2, ConstraintType.POINT, settings -> {
                    settings.setSpace(EConstraintSpace.WorldSpace);
                    settings.setPoint1(pivot);
                    settings.setPoint2(pivot);
                });
            }
        },
        HINGE {
            @Override
            void create(ConstraintManager manager, Body body1, @Nullable Body body2, RVec3 centre1, RVec3 centre2) {
                RVec3 pivot = midpoint(centre1, centre2);
                manager.createConstraint(body1, body2, ConstraintType.HINGE, settings -> {
                    settings.setSpace(EConstraintSpace.WorldSpace);
                    settings.setPoint1(pivot);
                    settings.setPoint2(pivot);
                    settings.setHingeAxis1(0f, 0f, 1f);
                    settings.setHingeAxis2(0f, 0f, 1f);
                    settings.setNormalAxis1(1f, 0f, 0f);
                    settings.setNormalAxis2(1f, 0f, 0f);
                });
            }
        },
        SLIDER {
            @Override
            void create(ConstraintManager manager, Body body1, @Nullable Body body2, RVec3 centre1, RVec3 centre2) {
                RVec3 pivot = midpoint(centre1, centre2);
                manager.createConstraint(body1, body2, ConstraintType.SLIDER, settings -> {
                    settings.setSpace(EConstraintSpace.WorldSpace);
                    settings.setPoint1(pivot);
                    settings.setPoint2(pivot);
                    settings.setSliderAxis1(0f, 1f, 0f);
                    settings.setSliderAxis2(0f, 1f, 0f);
                    settings.setNormalAxis1(1f, 0f, 0f);
                    settings.setNormalAxis2(1f, 0f, 0f);
                    settings.setLimitsMin(-0.5f);
                    settings.setLimitsMax(0.5f);
                });
            }
        },
        DISTANCE {
            @Override
            void create(ConstraintManager manager, Body body1, @Nullable Body body2, RVec3 centre1, RVec3 centre2) {
                manager.createConstraint(body1, body2, ConstraintType.DISTANCE, settings -> {
                    settings.setSpace(EConstraintSpace.WorldSpace);
                    settings.setPoint1(centre1);
                    settings.setPoint2(centre2);
                    settings.setMinDistance(0f);
                    settings.setMaxDistance((float) CHAIN_SPACING);
                });
            }
        };

        abstract void create(ConstraintManager manager, Body body1, @Nullable Body body2, RVec3 centre1, RVec3 centre2);

        String getName() {
            return name().toLowerCase(Locale.ROOT);
        }

        private static RVec3 midpoint(RVec3 a, RVec3 b) {
            return new RVec3((a.xx() + b.xx()) * 0.5, (a.yy() + b.yy()) * 0.5, (a.zz() + b.zz()) * 0.5);
        }
    }
}
