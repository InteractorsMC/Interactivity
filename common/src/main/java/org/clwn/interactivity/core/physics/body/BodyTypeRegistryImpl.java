package org.clwn.interactivity.core.physics.body;

import net.minecraft.resources.ResourceLocation;
import org.clwn.interactivity.api.physics.body.Body;
import org.clwn.interactivity.api.physics.body.BodyType;
import org.clwn.interactivity.api.physics.body.BodyTypeRegistry;
import org.clwn.interactivity.core.physics.behavior.BehaviorRegistryImpl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class BodyTypeRegistryImpl implements BodyTypeRegistry {
    public static final BodyTypeRegistryImpl INSTANCE = new BodyTypeRegistryImpl(BehaviorRegistryImpl.INSTANCE);

    private final BehaviorRegistryImpl behaviorRegistry;
    private final Map<ResourceLocation, BodyTypeImpl<? extends Body>> bodyTypes = new LinkedHashMap<>();

    private BodyTypeRegistryImpl(BehaviorRegistryImpl behaviorRegistry) {
        this.behaviorRegistry = behaviorRegistry;
    }

    @Override
    public synchronized <T extends Body> BodyType<T> registerBodyType(
            ResourceLocation typeId,
            BodyType.Factory<T> factory,
            Consumer<BodyType.Builder<T>> configure
    ) {
        if (typeId == null) {
            throw new IllegalArgumentException("typeId cannot be null");
        }
        if (factory == null) {
            throw new IllegalArgumentException("factory cannot be null");
        }
        if (configure == null) {
            throw new IllegalArgumentException("configure cannot be null");
        }
        if (bodyTypes.containsKey(typeId)) {
            throw new IllegalStateException("BodyType is already registered: " + typeId);
        }

        BodyTypeBuilderImpl<T> builder = new BodyTypeBuilderImpl<>();
        configure.accept(builder);

        BodyTypeImpl<T> bodyType = builder.build(typeId, factory, behaviorRegistry);
        bodyTypes.put(typeId, bodyType);
        return bodyType;
    }

    @Override
    public synchronized Optional<BodyType<? extends Body>> get(ResourceLocation typeId) {
        return Optional.ofNullable(bodyTypes.get(typeId));
    }

    @Override
    public synchronized boolean contains(ResourceLocation typeId) {
        return bodyTypes.containsKey(typeId);
    }

    @Override
    public synchronized Collection<BodyType<? extends Body>> getAll() {
        return bodyTypes.values().stream()
                .map(type -> (BodyType<? extends Body>) type)
                .collect(Collectors.toSet());
    }
}
