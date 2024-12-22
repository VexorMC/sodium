/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.caffeinemc.mods.sodium.mixin.features.render.frapi;

import dev.vexor.radium.compat.mojang.blaze3d.vertex.PoseStack;
import dev.vexor.radium.compat.mojang.blaze3d.vertex.VertexConsumer;
import dev.vexor.radium.compat.mojang.minecraft.BlockColors;
import dev.vexor.radium.compat.mojang.minecraft.random.SingleThreadedRandomSource;
import net.caffeinemc.mods.sodium.client.render.frapi.render.NonTerrainBlockRenderContext;
import dev.vexor.radium.frapi.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Entrypoint of the FRAPI pipeline for non-terrain block rendering, for the baked models that require it.
 */
@Mixin(BlockModelRenderer.class)
public abstract class ModelBlockRendererMixin {
    @Unique
    private final ThreadLocal<NonTerrainBlockRenderContext> contexts = ThreadLocal.withInitial(() -> new NonTerrainBlockRenderContext(BlockColors.INSTANCE));

    @Inject(method = "render(Lnet/minecraft/world/BlockView;Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/render/BufferBuilder;Z)Z", at = @At("HEAD"), cancellable = true)
    private void onRender(BlockView world, BakedModel model, BlockState state, BlockPos pos, BufferBuilder buffer, boolean cull, CallbackInfoReturnable<Boolean> cir) {
        if (!((FabricBakedModel) model).isVanillaAdapter()) {
            contexts.get().renderModel(world, model, state, pos, new PoseStack(), (VertexConsumer) buffer, cull, new SingleThreadedRandomSource(42L), 0L, 0);
            cir.cancel();
        }
    }
}
