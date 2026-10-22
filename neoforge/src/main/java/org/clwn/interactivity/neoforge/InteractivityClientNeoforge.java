package org.clwn.interactivity.neoforge;

import eu.midnightdust.lib.config.MidnightConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.clwn.interactivity.Interactivity;

@Mod(value = Interactivity.MOD_ID, dist = Dist.CLIENT)
public class InteractivityClientNeoforge {
    public InteractivityClientNeoforge(IEventBus eventBus) {
        ModLoadingContext.get().registerExtensionPoint(
                IConfigScreenFactory.class,
                () -> (client, parentScreen) -> MidnightConfig.getScreen(parentScreen, Interactivity.MOD_ID)
        );
    }

}
