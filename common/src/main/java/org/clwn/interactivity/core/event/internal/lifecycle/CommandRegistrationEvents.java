/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.event.internal.lifecycle;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.clwn.interactivity.core.event.internal.Event;

public class CommandRegistrationEvents {
    public static final Event<CommandRegistrationContext> REGISTER_EVENT = new Event<>();

    public record CommandRegistrationContext(CommandDispatcher<CommandSourceStack> dispatcher,
                                             CommandBuildContext buildContext,
                                             Commands.CommandSelection selection) {
    }
}
