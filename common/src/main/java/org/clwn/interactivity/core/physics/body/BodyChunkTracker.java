/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.body;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import org.clwn.interactivity.core.physics.body.store.SimulatedPhysicsStateContainer;

import java.util.List;

public class BodyChunkTracker {
    @Getter
    private final BodySpatialIndex spatialIndex = new BodySpatialIndex();

    private final List<BodyChunkListener> listeners = new ObjectArrayList<>();

    public static long chunkKeyOf(double x, double z) {
        return ChunkPos.asLong(Mth.floor(x) >> 4, Mth.floor(z) >> 4);
    }

    public void registerListener(BodyChunkListener listener) {
        listeners.add(listener);
    }

    public void onBodyAdded(int index, SimulatedPhysicsStateContainer container) {
        long chunkKey = chunkKeyOf(container.posX.get(index), container.posZ.get(index));
        container.chunkKey.put(index, chunkKey);
        spatialIndex.add(chunkKey, index);

        for (BodyChunkListener listener : listeners) {
            listener.onBodyAdded(index, chunkKey);
        }
    }

    public void onBodyRemoved(int index, SimulatedPhysicsStateContainer container) {
        long chunkKey = container.chunkKey.get(index);

        for (BodyChunkListener listener : listeners) {
            listener.onBodyRemoved(index, chunkKey);
        }

        spatialIndex.remove(chunkKey, index);
    }

    public void update(SimulatedPhysicsStateContainer container) {
        int dirtyCount = container.getDirtyCount();

        for (int k = 0; k < dirtyCount; k++) {
            int index = container.dirtyIndices.get(k);

            if (container.bodies[index] == null) {
                continue;
            }

            long oldKey = container.chunkKey.get(index);
            long newKey = chunkKeyOf(container.posX.get(index), container.posZ.get(index));

            if (oldKey == newKey) {
                continue;
            }

            container.chunkKey.put(index, newKey);
            spatialIndex.move(index, oldKey, newKey);

            for (BodyChunkListener listener : listeners) {
                listener.onBodyMoved(index, oldKey, newKey);
            }
        }
    }
}
