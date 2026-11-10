/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.event.internal.lifecycle;

import org.clwn.interactivity.core.event.internal.Event;

public final class ModLoaderLifecycleEvents {
    public static final Event<LoadCompleteEvent> LOAD_COMPLETE_EVENT = new Event<>();

    public record LoadCompleteEvent() {

    }
}
