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
package org.clwn.interactivity.builtin.body.block;

import com.github.stephengold.joltjni.BodyCreationSettings;
import com.github.stephengold.joltjni.BoxShapeSettings;
import com.github.stephengold.joltjni.ShapeSettings;
import com.github.stephengold.joltjni.Vec3;
import com.github.stephengold.joltjni.enumerate.EMotionType;
import org.clwn.interactivity.api.physics.ObjectLayer;
import org.clwn.interactivity.api.physics.PhysicsLayerRegistry;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.clwn.interactivity.api.physics.body.RigidBodyFactory;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;

import java.util.UUID;

public class BlockRigidBody extends Body {
    public BlockRigidBody(BodyType<? extends Body> type, PhysicsWorld world, UUID uuid) {
        super(type, world, uuid);
    }

    public static int createJoltBody(Body body, RigidBodyFactory factory) {
        try (
                ShapeSettings shapeSettings = new BoxShapeSettings(new Vec3(1f, 1f, 1f));
                BodyCreationSettings bcs = new BodyCreationSettings()
        ) {
            bcs.setMotionType(EMotionType.Dynamic);
            bcs.setObjectLayer(ObjectLayer.MOVING.getValue());
            return factory.create(shapeSettings, bcs);
        }
    }
}
