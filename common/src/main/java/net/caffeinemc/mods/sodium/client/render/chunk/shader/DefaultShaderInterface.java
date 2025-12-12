package net.caffeinemc.mods.sodium.client.render.chunk.shader;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlBuffer;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.*;
import net.caffeinemc.mods.sodium.client.gui.SodiumOptions;
import net.minecraft.client.MinecraftClient;
import org.joml.Matrix4fc;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;

import java.util.EnumMap;
import java.util.Map;

/**
 * A forward-rendering shader program for chunks.
 */
public class DefaultShaderInterface implements ChunkShaderInterface {
    private final Map<ChunkShaderTextureSlot, GlUniformInt> uniformTextures;

    private final GlUniformMatrix4f uniformModelViewMatrix;
    private final GlUniformMatrix4f uniformProjectionMatrix;
    private final GlUniformFloat3v uniformRegionOffset;
    private final GlUniformInt uniformCurrentTime;
    private final GlUniformFloat uniformFadePeriod;

    private final GlUniformBlock uniformChunkData;

    // The fog shader component used by this program in order to setup the appropriate GL state
    private final ChunkShaderFogComponent fogShader;

    public DefaultShaderInterface(ShaderBindingContext context, ChunkShaderOptions options) {
        this.uniformModelViewMatrix = context.bindUniform("u_ModelViewMatrix", GlUniformMatrix4f::new);
        this.uniformProjectionMatrix = context.bindUniform("u_ProjectionMatrix", GlUniformMatrix4f::new);
        this.uniformRegionOffset = context.bindUniform("u_RegionOffset", GlUniformFloat3v::new);

        this.uniformCurrentTime = context.bindUniform("u_CurrentTime", GlUniformInt::new);
        this.uniformFadePeriod = context.bindUniform("u_FadePeriodInv", GlUniformFloat::new);

        this.uniformChunkData = context.bindUniformBlock("ChunkData", 0);

        this.uniformTextures = new EnumMap<>(ChunkShaderTextureSlot.class);
        this.uniformTextures.put(ChunkShaderTextureSlot.BLOCK, context.bindUniform("u_BlockTex", GlUniformInt::new));
        this.uniformTextures.put(ChunkShaderTextureSlot.LIGHT, context.bindUniform("u_LightTex", GlUniformInt::new));

        this.fogShader = options.fog().getFactory().apply(context);
    }

    @Override // the shader interface should not modify pipeline state
    public void setupState() {
        // 36064
        this.bindTexture(ChunkShaderTextureSlot.BLOCK, 0);
        this.bindTexture(ChunkShaderTextureSlot.LIGHT, 1);

        uniformFadePeriod.setFloat((float) (1.0 / (SodiumClientMod.options().quality.chunkSectionFadeInTime * 1000.0))); // this is in seconds!

        this.fogShader.setup();
    }

    @Override // the shader interface should not modify pipeline state
    public void resetState() {
        // This is used by alternate implementations.
    }

    @Deprecated(forRemoval = true) // should be handled properly in GFX instead.
    private void bindTexture(ChunkShaderTextureSlot slot, int textureId) {
        GlStateManager.activeTexture(textureId); // this shouldn't be needed?

        var uniform = this.uniformTextures.get(slot);
        uniform.setInt(textureId);
    }

    @Override
    public void setChunkData(GlBuffer data, int time) {
        uniformChunkData.bindBuffer(data);
        uniformCurrentTime.set(time);
    }

    @Override
    public void setProjectionMatrix(Matrix4fc matrix) {
        this.uniformProjectionMatrix.set(matrix);
    }

    @Override
    public void setModelViewMatrix(Matrix4fc matrix) {
        this.uniformModelViewMatrix.set(matrix);
    }

    @Override
    public void setRegionOffset(float x, float y, float z) {
        this.uniformRegionOffset.set(x, y, z);
    }
}
