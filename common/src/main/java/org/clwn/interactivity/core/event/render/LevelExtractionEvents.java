package org.clwn.interactivity.core.event.render;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import org.clwn.interactivity.core.event.Event;

public class LevelExtractionEvents {
    public static final Event<LevelExtractionContext> EXTRACTION_END = new Event<>();

    public record LevelExtractionContext(LevelRenderer levelRenderer, Camera camera, Frustum frustum) {
    }
}
