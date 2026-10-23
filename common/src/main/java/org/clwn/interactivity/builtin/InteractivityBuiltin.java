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
package org.clwn.interactivity.builtin;

import org.clwn.interactivity.api.InteractivityAPI;
import org.clwn.interactivity.builtin.item.physicsgun.PhysicsGunLogic;
import org.clwn.interactivity.builtin.regsity.BodyRegistry;
import org.clwn.interactivity.builtin.regsity.RendererRegistry;
import org.clwn.interactivity.core.util.platform.Services;

public final class InteractivityBuiltin {
    public static void init() {
        BodyRegistry.register();

        InteractivityAPI.getInstance().getEventBus().registerListener(PhysicsGunLogic.class);

        if (Services.PLATFORM.isClientEnvironment()) {
            RendererRegistry.register();
        }
    }
}
