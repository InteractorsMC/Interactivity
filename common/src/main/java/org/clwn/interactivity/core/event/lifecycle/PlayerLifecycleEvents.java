package org.clwn.interactivity.core.event.lifecycle;

import net.minecraft.world.entity.player.Player;
import org.clwn.interactivity.core.event.Event;

public class PlayerLifecycleEvents {
    public static final Event<PlayerConfigureStateEvent> PLAYER_CONFIGURE_STATE_EVENT = new Event<>();

    // todo: actually configure something and add listeners
    public record PlayerConfigureStateEvent(Player player) {
    }

}
