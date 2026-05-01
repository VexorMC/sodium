package net.caffeinemc.mods.sodium.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import dev.vexor.radium.compat.mojang.minecraft.render.FogHelper;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import net.caffeinemc.mods.sodium.client.render.chunk.map.ChunkTracker;
import net.caffeinemc.mods.sodium.client.render.chunk.map.ChunkTrackerHolder;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.SortBehavior;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.trigger.CameraMovement;
import net.caffeinemc.mods.sodium.client.render.viewport.CameraTransform;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.caffeinemc.mods.sodium.client.services.PlatformRuntimeInformation;
import net.caffeinemc.mods.sodium.client.util.NativeBuffer;
import net.caffeinemc.mods.sodium.client.world.LevelRendererExtension;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BlockBreakingInfo;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import dev.vexor.radium.compat.mojang.minecraft.math.SectionPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL11;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Provides an extension to vanilla's {@link net.minecraft.client.render.WorldRenderer}.
 */
public class SodiumWorldRenderer {
    private final MinecraftClient client;

    private ClientWorld level;
    private int renderDistance;

    private Vector3d lastCameraPos;
    private double lastCameraPitch, lastCameraYaw;
    private float lastFogDistance;
    private Matrix4f lastProjectionMatrix;

    private boolean useEntityCulling;

    private RenderSectionManager renderSectionManager;

    /**
     * @return The SodiumWorldRenderer based on the current dimension
     */
    public static SodiumWorldRenderer instance() {
        var instance = instanceNullable();

        if (instance == null) {
            throw new IllegalStateException("No renderer attached to active level");
        }

        return instance;
    }

    /**
     * @return The SodiumWorldRenderer based on the current dimension, or null if none is attached
     */
    public static SodiumWorldRenderer instanceNullable() {
        if (MinecraftClient.getInstance().worldRenderer instanceof LevelRendererExtension extension) {
            return extension.sodium$getWorldRenderer();
        }

        return null;
    }

    public SodiumWorldRenderer(MinecraftClient client) {
        this.client = client;
    }

    public void setLevel(ClientWorld level) {
        // Check that the level is actually changing
        if (this.level == level) {
            return;
        }

        // If we have a level is already loaded, unload the renderer
        if (this.level != null) {
            this.unloadLevel();
        }

        // If we're loading a new level, load the renderer
        if (level != null) {
            this.loadLevel(level);
        }
    }

    private void loadLevel(ClientWorld level) {
        this.level = level;

        try (CommandList commandList = RenderDevice.INSTANCE.createCommandList()) {
            this.initRenderer(commandList);
        }
    }

    private void unloadLevel() {
        if (this.renderSectionManager != null) {
            this.renderSectionManager.destroy();
            this.renderSectionManager = null;
        }

        this.level = null;
    }

    /**
     * @return The number of chunk renders which are visible in the current camera's frustum
     */
    public int getVisibleChunkCount() {
        return this.renderSectionManager.getVisibleChunkCount();
    }

    /**
     * Notifies the chunk renderer that the graph scene has changed and should be re-computed.
     */
    public void scheduleTerrainUpdate() {
        // BUG: seems to be called before init
        if (this.renderSectionManager != null) {
            this.renderSectionManager.markGraphDirty();
        }
    }

    /**
     * @return True if no chunks are pending rebuilds
     */
    public boolean isTerrainRenderComplete() {
        return this.renderSectionManager.getBuilder().isBuildQueueEmpty();
    }

    /**
     * Called prior to any chunk rendering in order to update necessary state.
     */
    public void setupTerrain(Viewport viewport,
                             boolean spectator,
                             boolean updateChunksImmediately) {
        NativeBuffer.reclaim(false);

        this.processChunkEvents();

        this.useEntityCulling = SodiumClientMod.options().performance.useEntityCulling;

        if (this.client.options.viewDistance != this.renderDistance) {
            this.reload();
        }

        Profiler profiler = client.profiler;
        profiler.push("camera_setup");

        ClientPlayerEntity player = this.client.player;

        if (player == null) {
            throw new IllegalStateException("Client instance has no active player entity");
        }

        var cam = viewport.getTransform();
        var pos = new Vector3d(cam.x, cam.y, cam.z);
        Matrix4f projectionMatrix = new Matrix4f(Camera.PROJECTION_MATRIX);

        float pitch = Camera.getRotationX();
        float yaw = Camera.getRotationZ();
        float fogDistance = FogHelper.getFogEnd();

        if (this.lastCameraPos == null) {
            this.lastCameraPos = pos;
        }
        if (this.lastProjectionMatrix == null) {
            this.lastProjectionMatrix = new Matrix4f(projectionMatrix);
        }
        boolean cameraLocationChanged = !pos.equals(this.lastCameraPos);
        boolean cameraAngleChanged = pitch != this.lastCameraPitch || yaw != this.lastCameraYaw || fogDistance != this.lastFogDistance;
        boolean cameraProjectionChanged = !projectionMatrix.equals(this.lastProjectionMatrix, 0.0001f);

        this.lastProjectionMatrix = projectionMatrix;

        this.lastCameraPitch = pitch;
        this.lastCameraYaw = yaw;

        if (cameraLocationChanged || cameraAngleChanged || cameraProjectionChanged) {
            this.renderSectionManager.notifyChangedCamera();
        }

        this.lastFogDistance = fogDistance;

        this.renderSectionManager.prepareFrame(pos);

        if (cameraLocationChanged) {
            profiler.swap("translucent_triggering");

            this.renderSectionManager.processGFNIMovement(new CameraMovement(this.lastCameraPos, pos));
            this.lastCameraPos = pos;
        }

        int maxChunkUpdates = updateChunksImmediately ? this.renderDistance : 1;

        for (int i = 0; i < maxChunkUpdates; i++) {
            this.renderSectionManager.prepareRender();

            profiler.swap("chunk_render_lists");

            this.renderSectionManager.prepareRenderTrees(viewport, spectator);

            profiler.swap("chunk_update");

            this.renderSectionManager.cleanupAndFlip();
            this.renderSectionManager.updateChunks(viewport, updateChunksImmediately);

            profiler.swap("chunk_upload");

            this.renderSectionManager.processChunkBuilds(viewport);

            if (!this.renderSectionManager.needsUpdate()) {
                break;
            }
        }

        profiler.swap("chunk_render_lists");

        this.renderSectionManager.finalizeRenderLists(viewport, updateChunksImmediately);

        profiler.swap("chunk_render_tick");

        this.renderSectionManager.tickVisibleRenders();

        profiler.pop();
    }

    private void processChunkEvents() {
        this.renderSectionManager.beforeSectionUpdates();
        var tracker = ChunkTrackerHolder.get(this.level);
        tracker.forEachEvent(this.renderSectionManager::onChunkAdded, this.renderSectionManager::onChunkRemoved);
    }

    /**
     * Performs a render pass for the given {@link RenderLayer} and draws all visible chunks for it.
     */
    public void drawChunkLayer(RenderLayer renderLayer, ChunkRenderMatrices matrices, double x, double y, double z) {
        this.renderSectionManager.renderLayer(matrices, DefaultTerrainRenderPasses.fromLayer(renderLayer), x, y, z);
    }

    public void reload() {
        if (this.level == null) {
            return;
        }

        try (CommandList commandList = RenderDevice.INSTANCE.createCommandList()) {
            this.initRenderer(commandList);
        }
    }

    private void initRenderer(CommandList commandList) {
        if (this.renderSectionManager != null) {
            this.renderSectionManager = null;
        }

        // translucency sorting can be disabled in development environments by setting the debug option in the config file
        var sortBehavior = SortBehavior.DYNAMIC_DEFER_NEARBY_ZERO_FRAMES;

        if (PlatformRuntimeInformation.getInstance().isDevelopmentEnvironment()
                && !SodiumClientMod.options().debug.terrainSortingEnabled) {
            sortBehavior = SortBehavior.OFF;
        }

        this.renderDistance = this.client.options.viewDistance;

        this.renderSectionManager = new RenderSectionManager(this.level, this.renderDistance, sortBehavior, commandList);

        var tracker = ChunkTrackerHolder.get(this.level);
        ChunkTracker.forEachChunk(tracker.getReadyChunks(), this.renderSectionManager::onChunkAdded);
    }

    public void renderBlockEntities(Map<Integer, BlockBreakingInfo> blockBreakingProgressions, float tickDelta) {
        var dispatcher = BlockEntityRenderDispatcher.INSTANCE;

        this.renderGlobalBlockEntities(tickDelta, dispatcher);
        this.renderBlockEntities(blockBreakingProgressions, tickDelta, dispatcher);
    }

    private void renderBlockEntities(Map<Integer, BlockBreakingInfo> blockBreakingProgressions,
                                     float tickDelta,
                                     BlockEntityRenderDispatcher dispatcher) {
        SortedRenderLists renderLists = this.renderSectionManager.getRenderLists();
        Iterator<ChunkRenderList> renderListIterator = renderLists.iterator();

        while (renderListIterator.hasNext()) {
            var renderList = renderListIterator.next();

            var renderRegion = renderList.getRegion();
            var renderSectionIterator = renderList.sectionsWithEntitiesIterator();

            if (renderSectionIterator == null) {
                continue;
            }

            while (renderSectionIterator.hasNext()) {
                var renderSectionId = renderSectionIterator.nextByteAsInt();

                var blockEntities = renderRegion.getCulledBlockEntities(renderSectionId);

                if (blockEntities == null) {
                    continue;
                }

                for (BlockEntity blockEntity : blockEntities) {
                    dispatcher.renderEntity(blockEntity, tickDelta, -1);
                }
            }

            for (BlockBreakingInfo info : blockBreakingProgressions.values()) {
                var blockPos = info.getPos();
                var blockEntity = this.level.getBlockEntity(blockPos);

                if (blockEntity instanceof ChestBlockEntity chest) {
                    if (chest.neighborChestWest != null) {
                        blockPos = blockPos.offset(Direction.WEST);
                        blockEntity = this.level.getBlockEntity(blockPos);
                    } else if (chest.neighborChestNorth != null) {
                        blockPos = blockPos.offset(Direction.NORTH);
                        blockEntity = this.level.getBlockEntity(blockPos);
                    }
                }

                if (blockEntity == null) {
                    continue;
                }

                dispatcher.renderEntity(blockEntity, tickDelta, info.getStage());
            }
        }
    }

    private void renderGlobalBlockEntities(float tickDelta, BlockEntityRenderDispatcher dispatcher) {
        for (var renderSection : this.renderSectionManager.getSectionsWithGlobalEntities()) {
            var blockEntities = renderSection.getRegion().getGlobalBlockEntities(renderSection.getSectionIndex());

            if (blockEntities == null) {
                continue;
            }

            for (var blockEntity : blockEntities) {
                dispatcher.renderEntity(blockEntity, tickDelta, -1);
            }
        }
    }

    // the volume of a section multiplied by the number of sections to be checked at most
    private static final double MAX_ENTITY_CHECK_VOLUME = 16 * 16 * 16 * 50;

    /**
     * Returns whether or not the entity intersects with any visible chunks in the graph.
     * @return True if the entity is visible, otherwise false
     */
    public <T extends Entity> boolean isEntityVisible(T entity) {
        if (!this.useEntityCulling) {
            return true;
        }

        // Ensure entities with outlines or nametags are always visible
        if (entity.shouldRenderName()) {
            return true;
        }

        Box bb = entity.getBoundingBox();

        // bail on very large entities to avoid checking many sections
        double entityVolume = (bb.maxX - bb.minX) * (bb.maxY - bb.minY) * (bb.maxZ - bb.minZ);
        if (entityVolume > MAX_ENTITY_CHECK_VOLUME) {
            // large entities are only frustum tested, their sections are not checked for visibility
            return true;
        }

        return this.isBoxVisible(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
    }

    public boolean isBoxVisible(double x1, double y1, double z1, double x2, double y2, double z2) {
        return this.renderSectionManager.isBoxVisible(x1, y1, z1, x2, y2, z2);
    }

    public String getChunksDebugString() {
        // C: visible/total D: distance
        // TODO: add dirty and queued counts
        return String.format("C: %d/%d D: %d", this.renderSectionManager.getVisibleChunkCount(), this.renderSectionManager.getTotalSections(), this.renderDistance);
    }

    /**
     * Schedules chunk rebuilds for all chunks in the specified block region.
     */
    public void scheduleRebuildForBlockArea(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean important) {
        this.scheduleRebuildForChunks(minX >> 4, minY >> 4, minZ >> 4, maxX >> 4, maxY >> 4, maxZ >> 4, important);
    }

    /**
     * Schedules chunk rebuilds for all chunks in the specified chunk region.
     */
    public void scheduleRebuildForChunks(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean important) {
        for (int chunkX = minX; chunkX <= maxX; chunkX++) {
            for (int chunkY = minY; chunkY <= maxY; chunkY++) {
                for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ++) {
                    this.scheduleRebuildForChunk(chunkX, chunkY, chunkZ, important);
                }
            }
        }
    }

    /**
     * Schedules a chunk rebuild for the render belonging to the given chunk section position.
     */
    public void scheduleRebuildForChunk(int x, int y, int z, boolean important) {
        this.renderSectionManager.scheduleRebuild(x, y, z, important);
    }

    public Collection<String> getDebugStrings() {
        return this.renderSectionManager.getDebugStrings();
    }

    public boolean isSectionReady(int x, int y, int z) {
        return this.renderSectionManager.isSectionBuilt(x, y, z);
    }
}
