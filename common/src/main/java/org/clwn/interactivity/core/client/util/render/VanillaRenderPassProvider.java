package org.clwn.interactivity.core.client.util.render;

import net.minecraft.client.Minecraft;
import org.clwn.interactivity.api.client.util.render.RenderPassProvider;

public class VanillaRenderPassProvider implements RenderPassProvider {
    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public boolean isFirstPerson() {
        return Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }
}
