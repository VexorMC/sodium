package net.caffeinemc.mods.sodium.client.render.chunk.shader;

import net.caffeinemc.mods.sodium.client.gl.buffer.GlBuffer;
import org.joml.Matrix4fc;

public interface ChunkShaderInterface {
    @Deprecated
    void setupState();

    @Deprecated
    void resetState();

    void setProjectionMatrix(Matrix4fc matrix);

    void setModelViewMatrix(Matrix4fc matrix);

    void setRegionOffset(float x, float y, float z);

    void setChunkData(GlBuffer buffer, int time);
}
