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

import org.clwn.interactivity.api.client.InteractivityClientAPI;
import org.clwn.interactivity.builtin.body.block.BlockRenderer;

public class RendererRegistry {
    public static void register() {
        InteractivityClientAPI.getInstance().getBodyRendererRegistry().register(
                BodyRegistry.BLOCK_BODY,
                new BlockRenderer()
        );
    }
}
