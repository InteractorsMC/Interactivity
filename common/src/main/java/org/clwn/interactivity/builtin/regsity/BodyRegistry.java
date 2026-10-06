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
package org.clwn.interactivity.builtin.regsity;

import org.clwn.interactivity.api.InteractivityAPI;
import org.clwn.interactivity.api.physics.behavior.EventListenerBehavior;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.clwn.interactivity.builtin.body.block.BlockRigidBody;
import org.clwn.interactivity.core.util.InteractivityIdentifier;

public final class BodyRegistry {
    public static BodyType<BlockRigidBody> BLOCK_BODY;

    public static void register() {
        BLOCK_BODY = InteractivityAPI.getInstance().getBodyTypeRegistry().registerBodyType(
                InteractivityIdentifier.getIdentifier("block"),
                BlockRigidBody::new,
                (bodyBuilder -> bodyBuilder.addBehavior(
                        new EventListenerBehavior()
                ))
        );
    }
}
