package org.clwn.interactivity.util.platform;

import net.neoforged.fml.loading.LoadingModList;
import org.clwn.interactivity.util.platform.services.IPlatformHelper;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isModLoading(String modId) {
        return LoadingModList.get().getMods().stream().anyMatch(mod -> mod.getModId().equals(modId));
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }
}