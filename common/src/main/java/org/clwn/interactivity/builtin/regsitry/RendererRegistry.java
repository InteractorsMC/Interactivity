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
package org.clwn.interactivity.builtin.regsitry;

import org.clwn.interactivity.api.client.physics.render.RegisterBodyRenderersEvent;
import org.clwn.interactivity.api.event.SubscribeEvent;
import org.clwn.interactivity.builtin.body.block.BlockRenderer;

public class RendererRegistry {
    @SubscribeEvent
    public static void onRegisterRenderers(RegisterBodyRenderersEvent event) {
        event.register(BodyRegistry.BLOCK_BODY, new BlockRenderer());
        event.register(BodyRegistry.TEMPORARY_BLOCK_BODY, new BlockRenderer());
    }
}
