package org.clwn.interactivity.api.client.physics.world;

import net.minecraft.client.multiplayer.ClientLevel;
import org.clwn.interactivity.api.physics.world.PhysicsWorld;
import org.clwn.interactivity.api.util.Side;

public interface ClientPhysicsWorld extends PhysicsWorld {
    ClientLevel getClientLevel();

    @Override
    default Side getSide() {
        return Side.CLIENT;
    }
}
