package net.caffeinemc.mods.sodium.fabric.level;

import dev.lunasa.compat.mojang.blaze3d.vertex.VertexConsumer;
import net.caffeinemc.mods.sodium.client.services.PlatformLevelRenderHooks;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Matrix4f;

import java.util.List;
import java.util.function.Function;

public class FabricLevelRenderHooks implements PlatformLevelRenderHooks {

    @Override
    public void runChunkLayerEvents(RenderLayer renderLayer, WorldRenderer levelRenderer, Matrix4f modelMatrix, Matrix4f projectionMatrix, int ticks, net.minecraft.client.render.Camera mainCamera, Frustum cullingFrustum) {

    }

    @Override
    public List<?> retrieveChunkMeshAppenders(World level, BlockPos origin) {
        return List.of();
    }

    @Override
    public void runChunkMeshAppenders(List<?> renderers, Function<RenderLayer, VertexConsumer> typeToConsumer, LevelSlice slice) {

    }
}
