package gg.sona.radium.mixin.sodium.core.access;

import net.minecraft.block.Block;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Chunk.class)
public interface AChunk {
    @Invoker
    Block invokeGetBlock(int x, int y, int z);
}
