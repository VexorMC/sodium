package net.caffeinemc.mods.sodium.client.world.cloned;

import dev.lunasa.compat.mojang.minecraft.math.SectionPos;
import net.minecraft.util.math.Box;

import java.util.List;

public class ChunkRenderContext {
    private final SectionPos origin;
    private final ClonedChunkSection[] sections;
    private final Box volume;
    private final List<?> renderers;

    public ChunkRenderContext(SectionPos origin, ClonedChunkSection[] sections, Box volume, List<?> renderers) {
        this.origin = origin;
        this.sections = sections;
        this.volume = volume;
        this.renderers = renderers;
    }

    public ClonedChunkSection[] getSections() {
        return this.sections;
    }

    public SectionPos getOrigin() {
        return this.origin;
    }

    public Box getVolume() {
        return this.volume;
    }

    public List<?> getRenderers() {
        return renderers;
    }
}
