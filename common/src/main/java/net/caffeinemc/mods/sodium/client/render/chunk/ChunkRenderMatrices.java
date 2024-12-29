package net.caffeinemc.mods.sodium.client.render.chunk;

import net.minecraft.client.render.Camera;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public record ChunkRenderMatrices(Matrix4fc projection, Matrix4fc modelView) {

}
