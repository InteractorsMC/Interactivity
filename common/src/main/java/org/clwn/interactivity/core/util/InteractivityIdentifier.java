/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.util;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.clwn.interactivity.Interactivity;

public class InteractivityIdentifier {
    public static ResourceLocation getIdentifier(String path) {
        return ResourceLocation.fromNamespaceAndPath(Interactivity.MOD_ID, path);
    }

    public static Component getTranslation(String key, Object... args) {
        return Component.translatable(key, args);
    }
}
