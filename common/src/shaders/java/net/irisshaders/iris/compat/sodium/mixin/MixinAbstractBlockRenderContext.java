package net.irisshaders.iris.compat.sodium.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.frapi.render.AbstractBlockRenderContext;
import net.irisshaders.iris.platform.IrisPlatformHelpers;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBlockRenderContext.class)
public class MixinAbstractBlockRenderContext {
	@Shadow
	protected BlockPos pos;

	@Shadow
	protected BlockAndTintGetter level;

	@Inject(method = "bufferDefaultModel", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/services/PlatformModelAccess;getQuads(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/util/RandomSource;Lnet/minecraft/client/renderer/RenderType;Lnet/caffeinemc/mods/sodium/client/services/SodiumModelData;)Ljava/util/List;"))
	private void checkDirectionNeo(BakedModel model, BlockState state, CallbackInfo ci, @Local Direction cullFace) {
		if ((Object) this instanceof BlockRenderer r && WorldRenderingSettings.INSTANCE.getBlockStateIds() != null && cullFace != null) {
			BlockState appearance = IrisPlatformHelpers.getInstance().getBlockAppearance(this.level, state, cullFace, this.pos);
			((BlockSensitiveBufferBuilder) ((BlockRendererAccessor) r).getBuffers()).overrideBlock(WorldRenderingSettings.INSTANCE.getBlockStateIds().getInt(appearance));
		}
	}

	@Inject(method = "bufferDefaultModel", at = @At(value = "TAIL"))
	private void checkDirectionNeo(BakedModel model, BlockState state, CallbackInfo ci) {
		if ((Object) this instanceof BlockRenderer r && WorldRenderingSettings.INSTANCE.getBlockStateIds() != null) {
			((BlockSensitiveBufferBuilder) ((BlockRendererAccessor) r).getBuffers()).restoreBlock();
		}
	}
}
