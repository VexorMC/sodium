package net.caffeinemc.mods.sodium.mixin.features.render.frapi;

import dev.lunasa.compat.mojang.minecraft.random.RandomSource;
import net.caffeinemc.mods.sodium.client.render.frapi.render.AbstractBlockRenderContext;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Predicate;
import java.util.function.Supplier;

@Mixin(BakedModel.class)
public interface BakedModelMixin extends FabricBakedModel {
    @Override
    default void emitBlockQuads(QuadEmitter emitter, BlockView blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, Predicate<@Nullable Direction> cullTest) {
        if (emitter instanceof AbstractBlockRenderContext.BlockEmitter) {
            ((AbstractBlockRenderContext.BlockEmitter) emitter).bufferDefaultModel((BakedModel) this, state, cullTest);
        } else {
            FabricBakedModel.super.emitBlockQuads(emitter, blockView, state, pos, randomSupplier, cullTest);
        }
    }
}
