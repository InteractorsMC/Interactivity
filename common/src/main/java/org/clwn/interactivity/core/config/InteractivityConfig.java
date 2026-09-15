/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class InteractivityConfig extends MidnightConfig {
    public static final String PHYSICS = "physics";
    public static final String NETWORK = "network";
    public static final String DEV = "dev";

    public static final int AVAILABLE_CORES = Runtime.getRuntime().availableProcessors();

    @Entry(category = PHYSICS)
    public static boolean isClientSidePredictionEnabled = true;

    @Comment(category = NETWORK)
    public static Comment networkTransportComment;

    @Entry(
            category = NETWORK,
            min = -1,
            max = 5000,
            isSlider = true
    )
    public static int localTransport = 5000;

    @Entry(
            category = NETWORK,
            min = -1,
            max = 5000,
            isSlider = true
    )
    public static int udpTransport = 2000;

    @Entry(
            category = NETWORK,
            min = -1,
            max = 5000,
            isSlider = true
    )
    public static int tcpTransport = 0;

    @Entry(
            category = NETWORK,
            min = -1,
            max = 65535
    )
    public static int udpPort = -1;

    @Entry(
            category = PHYSICS,
            min = 1,
            max = 128,
            isSlider = true
    )
    public static int maxCoresAmount = AVAILABLE_CORES;

    @Entry(category = DEV)
    public static boolean uselessDevOption = false;
}
