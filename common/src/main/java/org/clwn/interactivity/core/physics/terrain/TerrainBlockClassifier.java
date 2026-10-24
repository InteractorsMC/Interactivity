/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.terrain;

import net.minecraft.world.level.block.state.BlockState;

/**
 * Assigns a tag to every block of the terrain.
 * <p>
 * Neighbouring full blocks are only merged into one box when their tags are equal, and every box keeps its tag
 * as the user data of its sub shape. Contacts with terrain can therefore tell what kind of block was hit,
 * e.g. to break leaves, without reading the level.
 * <p>
 * Called on the physics thread, so only data of the state itself may be used.
 */
@FunctionalInterface
public interface TerrainBlockClassifier {
    /**
     * Treats every block the same.
     */
    TerrainBlockClassifier VANILLA = state -> 0;

    int tag(BlockState state);
}
