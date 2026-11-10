package org.clwn.interactivity.core.client.physics.render;

import net.minecraft.resources.ResourceLocation;
import org.clwn.interactivity.api.client.physics.render.BodyRenderer;
import org.clwn.interactivity.api.client.physics.render.BodyRendererRegistry;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyType;

import java.util.LinkedHashMap;
import java.util.Map;

public class BodyRendererRegistryImpl implements BodyRendererRegistry {
    public static final BodyRendererRegistryImpl INSTANCE = new BodyRendererRegistryImpl();

    private final Map<ResourceLocation, BodyRenderer<?>> renderers = new LinkedHashMap<>();

    private boolean frozen;

    private BodyRendererRegistryImpl() {}

    public synchronized <B extends Body> void register(BodyType<B> bodyType, BodyRenderer<?> renderer) {
        if (frozen) {
            throw new IllegalStateException("Renderer registry is frozen");
        }        if (bodyType == null) {
            throw new IllegalArgumentException("bodyType cannot be null");
        }
        if (renderer == null) {
            throw new IllegalArgumentException("renderer cannot be null");
        }
        renderers.put(bodyType.getTypeId(), renderer);
    }

    public synchronized void freeze() {
        frozen = true;
    }

    @Override
    public BodyRenderer<?> getRenderer(ResourceLocation bodyTypeId) {
        return renderers.get(bodyTypeId);
    }
}
