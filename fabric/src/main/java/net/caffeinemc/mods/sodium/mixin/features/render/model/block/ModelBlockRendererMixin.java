package net.caffeinemc.mods.sodium.mixin.features.render.model.block;

import dev.vexor.radium.compat.mojang.blaze3d.vertex.PoseStack;
import dev.vexor.radium.compat.mojang.blaze3d.vertex.VertexConsumer;
import dev.vexor.radium.compat.mojang.minecraft.random.RandomSource;
import dev.vexor.radium.compat.mojang.minecraft.random.SingleThreadedRandomSource;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.client.model.quad.BakedQuadView;
import net.caffeinemc.mods.sodium.client.render.immediate.model.BakedModelEncoder;
import net.caffeinemc.mods.sodium.client.render.texture.SpriteUtil;
import net.caffeinemc.mods.sodium.client.render.vertex.VertexConsumerUtils;
import net.caffeinemc.mods.sodium.client.util.DirectionUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.TextureUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BlockModelRenderer.class)
public class ModelBlockRendererMixin {
    @Unique
    private final RandomSource random = new SingleThreadedRandomSource(42L);

    @Unique
    @SuppressWarnings("ForLoopReplaceableByForEach")
    private static void renderQuads(PoseStack.Pose matrices, VertexBufferWriter writer, int defaultColor, List<BakedQuad> quads, int light, int overlay) {
        for (int i = 0; i < quads.size(); i++) {
            BakedQuad bakedQuad = quads.get(i);

            if (bakedQuad.getVertexData().length < 32) {
                continue; // ignore bad quads
            }

            BakedQuadView quad = (BakedQuadView) bakedQuad;

            int color = quad.hasColor() ? defaultColor : 0xFFFFFFFF;

            BakedModelEncoder.writeQuadVertices(writer, matrices, quad, color, light, overlay, false);

            SpriteUtil.markSpriteActive(quad.getSprite());
        }
    }

    /**
     * @reason Use optimized vertex writer intrinsics, avoid allocations
     * @author JellySquid
     */
    @Inject(method = "render(Lnet/minecraft/world/BlockView;Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/render/BufferBuilder;Z)Z", at = @At("HEAD"), cancellable = true)
    private void renderFast(BlockView world, BakedModel model, BlockState state, BlockPos pos, BufferBuilder buffer, boolean cull, CallbackInfoReturnable<Boolean> cir) {
        var writer = VertexConsumerUtils.convertOrLog((VertexConsumer) buffer);
        if (writer == null) {
            return;
        }

        double x = (double)pos.getX();
        double y = (double)pos.getY();
        double z = (double)pos.getZ();
        Block.OffsetType offsetType = state.getBlock().getOffsetType();
        if (offsetType != Block.OffsetType.NONE) {
            long l = MathHelper.hashCode(pos);
            x += ((double)((float)(l >> 16 & 15L) / 15.0F) - (double)0.5F) * (double)0.5F;
            y += ((double)((float)(l >> 24 & 15L) / 15.0F) - (double)0.5F) * (double)0.5F;
            if (offsetType == Block.OffsetType.XYZ) {
                z += ((double)((float)(l >> 20 & 15L) / 15.0F) - (double)1.0F) * 0.2;
            }
        }

        cir.cancel();

        int i = state.getBlock().getColor(state.getBlock().getRenderState(state));
        if (GameRenderer.anaglyphEnabled) {
            i = TextureUtil.getAnaglyphColor(i);
        }

        float red = (float)(i >> 16 & 255) / 255.0F;
        float green = (float)(i >> 8 & 255) / 255.0F;
        float blue = (float)(i & 255) / 255.0F;

        RandomSource random = this.random;

        // Clamp color ranges
        red = MathHelper.clamp(red, 0.0F, 1.0F);
        green = MathHelper.clamp(green, 0.0F, 1.0F);
        blue = MathHelper.clamp(blue, 0.0F, 1.0F);

        int defaultColor = ColorABGR.pack(red, green, blue, 1.0F);

        PoseStack poseStack = new PoseStack();

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        for (Direction direction : DirectionUtil.ALL_DIRECTIONS) {
            random.setSeed(42L);
            List<BakedQuad> quads = model.getByDirection(direction);

            if (!quads.isEmpty()) {
                renderQuads(poseStack.last(), writer, defaultColor, quads, 0, 0);
            }
        }

        random.setSeed(42L);
        List<BakedQuad> quads = model.getQuads();

        if (!quads.isEmpty()) {
            renderQuads(poseStack.last(), writer, defaultColor, quads, 0, 0);
        }

        poseStack.popPose();
    }
}
