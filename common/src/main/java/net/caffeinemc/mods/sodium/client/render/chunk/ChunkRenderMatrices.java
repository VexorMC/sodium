package net.caffeinemc.mods.sodium.client.render.chunk;

import dev.vexor.radium.compat.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.render.Camera;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public record ChunkRenderMatrices(Matrix4fc projection, Matrix4fc modelView) {
    public static ChunkRenderMatrices from(PoseStack stack) {
        PoseStack.Pose entry = stack.last();
        return new ChunkRenderMatrices(new Matrix4f(Camera.PROJECTION_MATRIX), new Matrix4f(entry.pose()));
    }
}
