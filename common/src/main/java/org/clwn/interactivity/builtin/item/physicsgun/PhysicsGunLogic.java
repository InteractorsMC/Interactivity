/*
 * This file is dedicated to the public domain worldwide using the
 * Creative Commons CC0 1.0 Universal waiver.
 *
 * To the extent possible under law, the author(s) have waived all
 * copyright and related or neighboring rights to this file.
 * You can copy, modify, distribute, and perform the work, even for
 * commercial purposes, all without asking permission.
 *
 * For full legal text, see: https://creativecommons.org
 */
package org.clwn.interactivity.builtin.item.physicsgun;

import com.github.stephengold.joltjni.BodyInterface;
import com.github.stephengold.joltjni.Jolt;
import com.github.stephengold.joltjni.RRayCast;
import com.github.stephengold.joltjni.RVec3;
import com.github.stephengold.joltjni.RayCastResult;
import com.github.stephengold.joltjni.enumerate.EMotionType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.clwn.interactivity.api.InteractivityAPI;
import org.clwn.interactivity.api.physics.event.PhysicsTickEvent;
import org.clwn.interactivity.api.physics.event.SubscribeEvent;
import org.clwn.interactivity.api.physics.world.JoltBridge;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;
import org.clwn.interactivity.api.physics.world.ServerPhysicsWorld;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side logic of the physics gun.
 * <p>
 * The item (server thread) only toggles a grab and keeps the aim of the player up to date.
 * Everything that touches Jolt (picking the body, moving it) happens on the physics thread
 * in {@link #onPrePhysicsTick}.
 */
public final class PhysicsGunLogic {
    private static final double MAX_REACH = 64.0;
    private static final double MIN_HOLD_DISTANCE = 1.5;

    /**
     * Grab is dropped if the item did not report the aim for this long (item switched, player died or left).
     */
    private static final long AIM_TIMEOUT_NANOS = 500_000_000L;

    private static final double FOLLOW_SPEED = 10.0;
    private static final double MAX_SPEED = 40.0;
    private static final float ANGULAR_DAMPING = 0.9f;

    private static final Map<UUID, Grab> GRABS = new ConcurrentHashMap<>();

    private PhysicsGunLogic() {

    }

    private record Aim(Vec3 eye, Vec3 look) {
        static Aim of(ServerPlayer player) {
            return new Aim(player.getEyePosition(), player.getLookAngle());
        }
    }

    private static final class Grab {
        private final ServerPhysicsWorld world;
        private volatile Aim aim;
        private volatile long aimTimeNanos;
        private volatile boolean pickPending = true;

        // Physics thread only
        private int bodyId = Jolt.cInvalidBodyId;
        private double holdDistance;

        private Grab(ServerPhysicsWorld world, Aim aim) {
            this.world = world;
            updateAim(aim);
        }

        private void updateAim(Aim aim) {
            this.aim = aim;
            this.aimTimeNanos = System.nanoTime();
        }
    }

    /**
     * Starts a grab for the player or drops the current one. Called on the server thread.
     */
    public static void toggle(ServerPlayer player) {
        if (GRABS.remove(player.getUUID()) != null) {
            return;
        }

        ServerPhysicsWorld world = getWorld(player);
        if (world == null) {
            return;
        }

        GRABS.put(player.getUUID(), new Grab(world, Aim.of(player)));
    }

    /**
     * Called every server tick for every gun in the inventory of the player.
     *
     * @param held whether the gun is currently in one of the hands
     */
    public static void tick(ServerPlayer player, boolean held) {
        Grab grab = GRABS.get(player.getUUID());
        if (grab == null) {
            return;
        }

        if (!held || grab.world != getWorld(player)) {
            GRABS.remove(player.getUUID(), grab);
            return;
        }

        grab.updateAim(Aim.of(player));
    }

    private static ServerPhysicsWorld getWorld(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        PhysicsWorld world = InteractivityAPI.getInstance().getPhysicsPipeline().getPhysicsWorld(level);

        return world instanceof ServerPhysicsWorld serverWorld ? serverWorld : null;
    }

    @SubscribeEvent
    public static void onPrePhysicsTick(PhysicsTickEvent.PrePhysicsTickEvent event) {
        if (GRABS.isEmpty() || !(event.getPhysicsWorld() instanceof ServerPhysicsWorld world)) {
            return;
        }
        long now = System.nanoTime();

        for (Map.Entry<UUID, Grab> entry : GRABS.entrySet()) {
            Grab grab = entry.getValue();
            if (grab.world != world) {
                continue;
            }

            if (now - grab.aimTimeNanos > AIM_TIMEOUT_NANOS) {
                GRABS.remove(entry.getKey(), grab);
                continue;
            }

            if (grab.pickPending) {
                grab.pickPending = false;
                if (!pick(world.getJoltBridge(), grab)) {
                    GRABS.remove(entry.getKey(), grab);
                    continue;
                }
            }

            if (!hold(world.getJoltBridge(), grab)) {
                GRABS.remove(entry.getKey(), grab);
            }
        }
    }

    /**
     * Casts a ray from the eyes of the player and remembers the dynamic body it hit.
     */
    private static boolean pick(JoltBridge joltBridge, Grab grab) {
        Aim aim = grab.aim;
        Vec3 eye = aim.eye();
        Vec3 look = aim.look();

        try (
                RRayCast ray = new RRayCast(
                        eye.x, eye.y, eye.z,
                        (float) (look.x * MAX_REACH), (float) (look.y * MAX_REACH), (float) (look.z * MAX_REACH)
                );
                RayCastResult result = new RayCastResult()
        ) {
            if (!joltBridge.getNarrowPhaseQuery().castRay(ray, result)) {
                return false;
            }

            int bodyId = result.getBodyId();
            if (joltBridge.getBodyInterface().getMotionType(bodyId) != EMotionType.Dynamic) {
                return false;
            }

            grab.bodyId = bodyId;
            grab.holdDistance = Math.max(MIN_HOLD_DISTANCE, result.getFraction() * MAX_REACH);
            return true;
        }
    }

    /**
     * Moves the grabbed body to the point in front of the player.
     *
     * @return {@code false} if the body is gone and the grab should be dropped
     */
    private static boolean hold(JoltBridge joltBridge, Grab grab) {
        BodyInterface bodyInterface = joltBridge.getBodyInterface();
        int bodyId = grab.bodyId;

        if (!bodyInterface.isAdded(bodyId)) {
            return false;
        }

        Vec3 target = grab.aim.eye().add(grab.aim.look().scale(grab.holdDistance));
        RVec3 position = bodyInterface.getCenterOfMassPosition(bodyId);

        double vx = (target.x - position.xx()) * FOLLOW_SPEED;
        double vy = (target.y - position.yy()) * FOLLOW_SPEED;
        double vz = (target.z - position.zz()) * FOLLOW_SPEED;

        double speed = Math.sqrt(vx * vx + vy * vy + vz * vz);
        if (speed > MAX_SPEED) {
            double scale = MAX_SPEED / speed;
            vx *= scale;
            vy *= scale;
            vz *= scale;
        }

        bodyInterface.activateBody(bodyId);
        bodyInterface.setLinearVelocity(bodyId, (float) vx, (float) vy, (float) vz);

        com.github.stephengold.joltjni.Vec3 angularVelocity = bodyInterface.getAngularVelocity(bodyId);
        bodyInterface.setAngularVelocity(
                bodyId,
                angularVelocity.getX() * ANGULAR_DAMPING,
                angularVelocity.getY() * ANGULAR_DAMPING,
                angularVelocity.getZ() * ANGULAR_DAMPING
        );

        return true;
    }
}
