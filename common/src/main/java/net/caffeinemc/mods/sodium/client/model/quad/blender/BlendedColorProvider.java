package net.caffeinemc.mods.sodium.client.model.quad.blender;

import dev.vexor.radium.compat.mojang.math.Mth;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.model.color.ColorProvider;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.caffeinemc.mods.sodium.api.util.ColorMixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public abstract class BlendedColorProvider implements ColorProvider {
    @Override
    public void getColors(LevelSlice slice, BlockPos pos, ModelQuadView quad, int[] output) {
        for (int vertexIndex = 0; vertexIndex < 4; vertexIndex++) {
            output[vertexIndex] = this.getVertexColor(slice, pos, quad, vertexIndex);
        }
    }

    private int getVertexColor(LevelSlice slice, BlockPos pos, ModelQuadView quad, int vertexIndex) {
        BlockPos.Mutable scratchPos = new BlockPos.Mutable();

        // The vertex position
        // We add a half-texel offset since we are sampling points within a color texture
        final float x = quad.getX(vertexIndex) - 0.5f;
        final float y = quad.getY(vertexIndex) - 0.5f;
        final float z = quad.getZ(vertexIndex) - 0.5f;

        // Integer component of vertex position
        final int intX = MathHelper.floor(x);
        final int intY = MathHelper.floor(y);
        final int intZ = MathHelper.floor(z);

        // Fractional component of vertex position
        final float fracX = x - intX;
        final float fracY = y - intY;
        final float fracZ = z - intZ;

        // Block coordinates (in world space) which the vertex is located within
        // This is calculated after converting from floating point to avoid precision loss with large coordinates
        final int blockX = pos.getX() + intX;
        final int blockY = pos.getY() + intY;
        final int blockZ = pos.getZ() + intZ;

        // Retrieve the color values for each neighboring value
        // This creates a 2x2 matrix which is then sampled during interpolation
        final int m00 = this.getColor(slice, scratchPos.setPosition(blockX + 0, blockY, blockZ + 0));
        final int m01 = this.getColor(slice, scratchPos.setPosition(blockX + 0, blockY, blockZ + 1));
        final int m10 = this.getColor(slice, scratchPos.setPosition(blockX + 1, blockY, blockZ + 0));
        final int m11 = this.getColor(slice, scratchPos.setPosition(blockX + 1, blockY, blockZ + 1));

        // Perform interpolation across the X-axis, and then Y-axis
        // y0 = (m00 * (1.0 - x)) + (m10 * x)
        // y1 = (m01 * (1.0 - x)) + (m11 * x)
        // result = (y0 * (1.0 - y)) + (y1 * y)
        return ColorMixer.mix2d(m00, m01, m10, m11, fracX, fracZ);
    }

    protected abstract int getColor(LevelSlice slice, BlockPos pos);
}
