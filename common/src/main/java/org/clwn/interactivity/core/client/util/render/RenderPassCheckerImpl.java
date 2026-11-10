package org.clwn.interactivity.core.client.util.render;

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
        return renderPassProviders.stream().anyMatch(provider -> provider.isActive() && provider.isFirstPerson());
    }
}
