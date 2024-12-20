package net.caffeinemc.mods.sodium.client.services;

import dev.vexor.radium.compat.mojang.blaze3d.vertex.VertexConsumer;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Matrix4f;

import java.util.List;
import java.util.function.Function;

public interface PlatformLevelRenderHooks {
    PlatformLevelRenderHooks INSTANCE = Services.load(PlatformLevelRenderHooks.class);

    static PlatformLevelRenderHooks getInstance() {
        return INSTANCE;
    }

    /**
     * Runs any events after drawing a chunk layer.
     * @param renderLayer The current chunk layer that was drawn
     * @param levelRenderer The level renderer
     * @param modelMatrix The current modelview matrix
     * @param projectionMatrix The current projection matrix
     * @param ticks The current tick count
     * @param mainCamera The current camera
     * @param cullingFrustum The current frustum
     */
    void runChunkLayerEvents(RenderLayer renderLayer, WorldRenderer levelRenderer, Matrix4f modelMatrix, Matrix4f projectionMatrix, int ticks, Camera mainCamera, Frustum cullingFrustum);

    /**
     * Returns any NeoForge chunk renderers to run. <b>This is not thread safe.</b>
     * @param level The current level
     * @param origin The origin of the current chunk
     * @return Any NeoForge chunk renderers to run
     */
    List<?> retrieveChunkMeshAppenders(World level, BlockPos origin);

    /**
     * Runs any NeoForge chunk renderers.
     * @param renderers The list of chunk renderers to run.
     * @param typeToConsumer A consumer that converts render types to vertex consumers
     * @param slice The current level slice
     */
    void runChunkMeshAppenders(List<?> renderers, Function<RenderLayer, VertexConsumer> typeToConsumer, LevelSlice slice);
}
