package org.clwn.interactivity.core.client.physics.body;

import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyManager;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.clwn.interactivity.core.physics.body.store.AbstractBodyDataStore;
import org.clwn.interactivity.core.physics.network.BodyStateCodec;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface ClientBodyManager extends BodyManager {
    AbstractBodyDataStore getBodyDataStore();

    void addRemote(int networkId, UUID uuid, BodyType<? extends Body> type, BodyStateCodec.Record state, byte[] data, int tick);
    void removeRemote(int networkId);
    void applyState(BodyStateCodec.Record state, int tick);

    @Nullable Body getBodyByNetworkId(int networkId);
}
