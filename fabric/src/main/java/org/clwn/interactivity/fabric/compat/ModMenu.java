package org.clwn.interactivity.fabric.compat;

import com.terraformersmc.modmenu.ModMenuModMenuCompat;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import eu.midnightdust.lib.config.MidnightConfig;
import org.clwn.interactivity.Interactivity;

public class ModMenu extends ModMenuModMenuCompat {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> MidnightConfig.getScreen(parentScreen, Interactivity.MOD_ID);
    }
}
