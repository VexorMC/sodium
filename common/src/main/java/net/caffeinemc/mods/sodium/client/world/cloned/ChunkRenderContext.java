package net.caffeinemc.mods.sodium.client.world.cloned;

import dev.vexor.radium.compat.mojang.minecraft.math.SectionPos;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.Box;

import java.util.List;

public record ChunkRenderContext(SectionPos origin, ClonedChunkSection[] sections, BlockBox volume) {
}
