package net.caffeinemc.mods.sodium.client.world.cloned;

import dev.lunasa.compat.mojang.minecraft.math.SectionPos;
import net.minecraft.util.math.Box;

import java.util.List;

public record ChunkRenderContext(SectionPos origin, ClonedChunkSection[] sections, Box volume, List<?> renderers) {
}
