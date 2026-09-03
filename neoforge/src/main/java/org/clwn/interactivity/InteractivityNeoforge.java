package org.clwn.interactivity;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Interactivity.MOD_ID)
public class InteractivityNeoforge {

    public InteractivityNeoforge(IEventBus eventBus) {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        Interactivity.LOGGER.info("Hello NeoForge world!");
        Interactivity.init();

    }
}