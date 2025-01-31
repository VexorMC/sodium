package net.coderbot.iris.mixin.vertices.block_rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.vertices.BlockSensitiveBufferBuilder;
import net.coderbot.iris.vertices.ExtendedDataHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 * Captures and tracks the current block being rendered.
 *
 * Uses a priority of 999 so that we apply before Indigo's mixins.
 */
@Mixin(BlockModelRenderer.class)
public class MixinChunkRebuildTask {
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

    @Inject(method = "render(Lnet/minecraft/world/BlockView;Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/render/BufferBuilder;Z)Z", at = @At(value = "HEAD"))
	private void iris$onRenderBlock(BlockView world, BakedModel model, BlockState state, BlockPos pos, BufferBuilder buffer, boolean cull, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (buffer instanceof BlockSensitiveBufferBuilder) {
                lastBufferBuilder = ((BlockSensitiveBufferBuilder) buffer);
                lastBufferBuilder.beginBlock(resolveBlockId(state), ExtendedDataHelper.BLOCK_RENDER_TYPE, pos.getX() & 0xF, pos.getY() & 0xF, pos.getZ() & 0xF);
            }
        } catch (Throwable e) {}
	}

    @Inject(method = "render(Lnet/minecraft/world/BlockView;Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/render/BufferBuilder;Z)Z", at = @At(value = "RETURN"))
	private void iris$finishRenderingBlock(BlockView world, BakedModel model, BlockState state, BlockPos pos, BufferBuilder buffer, boolean cull, CallbackInfoReturnable<Boolean> cir) {
		if (lastBufferBuilder != null) {
			lastBufferBuilder.endBlock();
			lastBufferBuilder = null;
		}
	}
}
