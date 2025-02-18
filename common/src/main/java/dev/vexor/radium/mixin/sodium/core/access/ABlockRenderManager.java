package dev.vexor.radium.mixin.sodium.core.access;

import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.FluidRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockRenderManager.class)
public interface ABlockRenderManager {
    @Accessor
    FluidRenderer getFluidRenderer();
}
