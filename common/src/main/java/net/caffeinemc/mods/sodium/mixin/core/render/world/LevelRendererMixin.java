package net.caffeinemc.mods.sodium.mixin.core.render.world;

import dev.lunasa.compat.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.render.viewport.ViewportProvider;
import net.caffeinemc.mods.sodium.client.services.PlatformLevelRenderHooks;
import net.caffeinemc.mods.sodium.client.util.FlawlessFrames;
import net.caffeinemc.mods.sodium.client.world.LevelRendererExtension;
import net.caffeinemc.mods.sodium.mixin.core.access.CameraAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(WorldRenderer.class)
public abstract class LevelRendererMixin implements LevelRendererExtension {
    @Shadow
    private int ticks;

    @Shadow
    @Final
    private Map<Integer, BlockBreakingInfo> blockBreakingInfos;
    @Unique
    private SodiumWorldRenderer renderer;

    @Override
    public SodiumWorldRenderer sodium$getWorldRenderer() {
        return this.renderer;
    }

    @Redirect(method = "reload()V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;viewDistance:I", ordinal = 1))
    private int nullifyBuiltChunkStorage(GameOptions instance) {
        // Do not allow any resources to be allocated
        return 0;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(MinecraftClient client, CallbackInfo ci) {
        this.renderer = new SodiumWorldRenderer(client);
    }

    @Inject(method = "setWorld", at = @At("RETURN"))
    private void onWorldChanged(ClientWorld level, CallbackInfo ci) {
        RenderDevice.enterManagedCode();

        try {
            this.renderer.setLevel(level);
        } finally {
            RenderDevice.exitManagedCode();
        }
    }


    @Inject(method = "scheduleTerrainUpdate", at = @At("RETURN"))
    private void onTerrainUpdateScheduled(CallbackInfo ci) {
        this.renderer.scheduleTerrainUpdate();
    }

    /**
     * @reason Redirect the chunk layer render passes to our renderer
     * @author JellySquid
     */
    @Overwrite
    public int renderLayer(RenderLayer renderLayer, double tickDelta, int anaglyphFilter, Entity entity) {
        RenderDevice.enterManagedCode();

        double x = entity.prevTickX + (entity.x - entity.prevTickX) * tickDelta;
        double y = entity.prevTickY + (entity.y - entity.prevTickY) * tickDelta;
        double z = entity.prevTickZ + (entity.z - entity.prevTickZ) * tickDelta;

        Matrix4f projectionMatrix = new Matrix4f(CameraAccessor.getProjectionMatrix());
        Matrix4f modelViewMatrix = new Matrix4f(CameraAccessor.getModelMatrix());

        modelViewMatrix.translate(0f, 16f, 0f);

        try {
            this.renderer.drawChunkLayer(renderLayer, new ChunkRenderMatrices(projectionMatrix, modelViewMatrix), x, y, z);
        } finally {
            RenderDevice.exitManagedCode();
        }

        return 0;

    }

    /**
     * This seems to fix the weird crashes with LWJGL complaining about a
     * buffer being used when an array buffer is bound.
     */
    @Inject(method = "renderSky", at = @At(value = "HEAD"))
    public void renderSky(CallbackInfo ci) {
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    /**
     * @reason Redirect the terrain setup phase to our renderer
     * @author JellySquid
     */
    @Overwrite
    public void setupTerrain(Entity entity, double tickDelta, CameraView cameraView, int frame, boolean spectator) {
        var viewport = ((ViewportProvider) Frustum.getInstance()).sodium$createViewport();
        var updateChunksImmediately = FlawlessFrames.isActive();

        RenderDevice.enterManagedCode();

        try {
            this.renderer.setupTerrain(viewport, spectator, true);
        } finally {
            RenderDevice.exitManagedCode();
        }
    }
    /**
     * @reason Redirect chunk updates to our renderer
     * @author JellySquid
     */
    @Overwrite
    public void updateBlock(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.renderer.scheduleRebuildForBlockArea(minX, minY, minZ, maxX, maxY, maxZ, false);
    }

    /**
     * @reason Redirect chunk updates to our renderer
     * @author JellySquid
     */
    @Overwrite
    public void onRenderRegionUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
        this.renderer.scheduleRebuildForChunks(x1, y1, z1, x2, y2, z2, false);
    }

    /**
     * @reason Redirect the updates to our renderer
     * @author JellySquid
     */
    @Overwrite
    public void updateChunks(long p) {
    }

    /**
     * @reason Redirect chunk updates to our renderer
     * @author JellySquid
     */
    @Overwrite
    public void onBlockUpdate(BlockPos pos) {
        this.renderer.scheduleRebuildForBlockArea(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, false);
    }

    @Inject(method = "reload()V", at = @At("RETURN"))
    private void onReload(CallbackInfo ci) {
        RenderDevice.enterManagedCode();

        try {
            this.renderer.reload();
        } finally {
            RenderDevice.exitManagedCode();
        }
    }

    @Inject(method = "renderEntities", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/WorldRenderer;noCullingBlockEntities:Ljava/util/Set;", shift = At.Shift.BEFORE, ordinal = 1))
    private void onRenderBlockEntities(Entity entity, CameraView cameraView, float tickDelta, CallbackInfo ci) {
        this.renderer.renderBlockEntities(this.blockBreakingInfos, tickDelta);
    }
}
