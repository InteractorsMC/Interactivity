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

    private BodyRendererRegistryImpl() {}

    @Override
    public synchronized <B extends Body, R extends BodyRenderer<?>> void register(BodyType<B> bodyType, R renderer) {
        if (bodyType == null) {
            throw new IllegalArgumentException("bodyType cannot be null");
        }
        if (renderer == null) {
            throw new IllegalArgumentException("renderer cannot be null");
        }
        renderers.put(bodyType.getTypeId(), renderer);
    }

    @Override
    public BodyRenderer<?> getRenderer(ResourceLocation bodyTypeId) {
        return renderers.get(bodyTypeId);
    }
}
