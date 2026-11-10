package org.clwn.interactivity.core.client.util.render;

import net.minecraft.client.Minecraft;
import org.clwn.interactivity.api.client.util.render.RenderPassChecker;
import org.clwn.interactivity.api.client.util.render.RenderPassProvider;

import java.util.List;
import java.util.ServiceLoader;

public class RenderPassCheckerImpl implements RenderPassChecker {
    public static RenderPassCheckerImpl INSTANCE = new RenderPassCheckerImpl();

    List<RenderPassProvider> renderPassProviders;

    private RenderPassCheckerImpl() {
        renderPassProviders = ServiceLoader.load(RenderPassProvider.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .toList();
    }

    @Override
    public boolean isFirstPerson() {
        var activeProviders = renderPassProviders.stream()
                .filter(RenderPassProvider::isActive)
                .toList();

        return activeProviders.isEmpty()
                ? Minecraft.getInstance().options.getCameraType().isFirstPerson()
                : activeProviders.stream().anyMatch(RenderPassProvider::isFirstPerson);
    }

    @Override
    public boolean isThirdPerson() {
        var activeProviders = renderPassProviders.stream()
                .filter(RenderPassProvider::isActive)
                .toList();

        return activeProviders.isEmpty()
                ? !Minecraft.getInstance().options.getCameraType().isFirstPerson()
                : activeProviders.stream().anyMatch(RenderPassProvider::isThirdPerson);
    }
}
