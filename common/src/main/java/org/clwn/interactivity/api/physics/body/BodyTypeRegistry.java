package org.clwn.interactivity.api.physics.body;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

public interface BodyTypeRegistry {
    <T extends Body> BodyType<T> registerBodyType(
            ResourceLocation typeId,
            BodyType.Factory<T> factory,
            Consumer<BodyType.Builder<T>> configure
    );

    Optional<BodyType<? extends Body>> get(ResourceLocation typeId);

    boolean contains(ResourceLocation typeId);

    Collection<BodyType<? extends Body>> getAll();
}
