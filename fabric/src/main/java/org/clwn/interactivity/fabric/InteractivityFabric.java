package org.clwn.interactivity.fabric;

import net.fabricmc.api.ModInitializer;
import org.clwn.interactivity.Interactivity;

public class InteractivityFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        
        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.
        Interactivity.LOGGER.info("Hello Fabric world!");
        Interactivity.init();
    }
}
