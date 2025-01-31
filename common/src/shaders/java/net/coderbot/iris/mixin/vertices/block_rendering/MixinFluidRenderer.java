package net.coderbot.iris.mixin.vertices.block_rendering;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.vertices.BlockSensitiveBufferBuilder;
import net.coderbot.iris.vertices.ExtendedDataHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Captures and tracks the current block being rendered.
 *
 * Uses a priority of 999 so that we apply before Indigo's mixins.
 */
@Mixin(FluidRenderer.class)
public class MixinFluidRenderer {
	@Unique
	private BlockSensitiveBufferBuilder lastBufferBuilder;

	// Resolve the ID map on the main thread to avoid thread safety issues
	@Unique
	private final Object2IntMap<BlockState> blockStateIds = getBlockStateIds();

	@Unique
	private Object2IntMap<BlockState> getBlockStateIds() {
		return BlockRenderingSettings.INSTANCE.getBlockStateIds();
	}

	@Unique
	private short resolveBlockId(BlockState state) {
		if (blockStateIds == null) {
			return -1;
		}

		return (short) blockStateIds.getOrDefault(state, -1);
	}

	@Inject(method = "render", at = @At(value = "HEAD"))
	private void iris$onRenderLiquid(BlockView world, BlockState state, BlockPos pos, BufferBuilder buffer, CallbackInfoReturnable<Boolean> cir) {
		if (buffer instanceof BlockSensitiveBufferBuilder) {
			lastBufferBuilder = ((BlockSensitiveBufferBuilder) buffer);
			// TODO: We're using createLegacyBlock? That seems like something that Mojang wants to deprecate.
			lastBufferBuilder.beginBlock(resolveBlockId(state), ExtendedDataHelper.FLUID_RENDER_TYPE, pos.getX() & 0xF, pos.getY() & 0xF, pos.getZ() & 0xF);
		}
	}

    @Inject(method = "render", at = @At(value = "RETURN"))
	private void iris$finishRenderingLiquid(BlockView world, BlockState state, BlockPos pos, BufferBuilder buffer, CallbackInfoReturnable<Boolean> cir) {
		if (lastBufferBuilder != null) {
			lastBufferBuilder.endBlock();
			lastBufferBuilder = null;
		}
	}
}
