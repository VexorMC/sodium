package net.caffeinemc.mods.sodium.client.render;

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
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.trigger.CameraMovement;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.caffeinemc.mods.sodium.client.util.NativeBuffer;
import net.caffeinemc.mods.sodium.client.world.LevelRendererExtension;
import net.minecraft.block.entity.BlockEntity;
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
        // if (this.level != null) {
        //    this.unloadLevel();
        // }

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

        Vec3d posRaw = Camera.getPosition();
        Vector3d pos = new Vector3d(posRaw.x, posRaw.y, posRaw.z);
        Matrix4f projectionMatrix = new Matrix4f(Camera.PROJECTION_MATRIX);

        float pitch = Camera.getRotationZ();
        float yaw = Camera.getRotationX();
        float fogDistance = FogHelper.getFogEnd();

        if (this.lastCameraPos == null) {
            this.lastCameraPos = new Vector3d(pos);
        }
        if (this.lastProjectionMatrix == null) {
            this.lastProjectionMatrix = new Matrix4f(projectionMatrix);
        }
        boolean cameraLocationChanged = !pos.equals(this.lastCameraPos);
        boolean cameraAngleChanged = pitch != this.lastCameraPitch || yaw != this.lastCameraYaw || fogDistance != this.lastFogDistance;
        boolean cameraProjectionChanged = !projectionMatrix.equals(this.lastProjectionMatrix);

        this.lastProjectionMatrix = projectionMatrix;

        this.lastCameraPitch = pitch;
        this.lastCameraYaw = yaw;

        if (cameraLocationChanged || cameraAngleChanged || cameraProjectionChanged) {
            this.renderSectionManager.markGraphDirty();
        }

        this.lastFogDistance = fogDistance;

        this.renderSectionManager.updateCameraState(pos);

        if (cameraLocationChanged) {
            profiler.swap("translucent_triggering");

            this.renderSectionManager.processGFNIMovement(new CameraMovement(this.lastCameraPos, pos));
            this.lastCameraPos = new Vector3d(pos);
        }

        int maxChunkUpdates = updateChunksImmediately ? this.renderDistance : 1;

        for (int i = 0; i < maxChunkUpdates; i++) {
            if (this.renderSectionManager.needsUpdate()) {
                profiler.swap("chunk_render_lists");

                this.renderSectionManager.update(viewport, spectator);
            }

            profiler.swap("chunk_update");

            this.renderSectionManager.cleanupAndFlip();
            this.renderSectionManager.updateChunks(updateChunksImmediately);

            profiler.swap("chunk_upload");

            this.renderSectionManager.uploadChunks();

            if (!this.renderSectionManager.needsUpdate()) {
                break;
            }
        }

        profiler.swap("chunk_render_tick");

        this.renderSectionManager.tickVisibleRenders();

        profiler.pop();
    }

    private void processChunkEvents() {
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

        this.renderDistance = this.client.options.viewDistance;

        this.renderSectionManager = new RenderSectionManager(this.level, this.renderDistance, commandList);

        var tracker = ChunkTrackerHolder.get(this.level);
        ChunkTracker.forEachChunk(tracker.getReadyChunks(), this.renderSectionManager::onChunkAdded);
    }

    public void renderBlockEntities(Map<Integer, BlockBreakingInfo> blockBreakingProgressions,
                                    float tickDelta) {
        Vec3d cameraPos = Camera.getPosition();
        double x = cameraPos.x;
        double y = cameraPos.y;
        double z = cameraPos.z;

        ClientPlayerEntity player = this.client.player;

        if (player == null) {
            throw new IllegalStateException("Client instance has no active player entity");
        }

        BlockEntityRenderDispatcher blockEntityRenderer = BlockEntityRenderDispatcher.INSTANCE;

        this.renderBlockEntities(blockBreakingProgressions, tickDelta, x, y, z, blockEntityRenderer, player);
        this.renderGlobalBlockEntities(blockBreakingProgressions, tickDelta, x, y, z, blockEntityRenderer, player);
    }

    private void renderBlockEntities(Map<Integer, BlockBreakingInfo> blockBreakingProgressions,
                                     float tickDelta,
                                     double x,
                                     double y,
                                     double z,
                                     BlockEntityRenderDispatcher blockEntityRenderer,
                                     ClientPlayerEntity player) {
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
                var renderSection = renderRegion.getSection(renderSectionId);

                var blockEntities = renderSection.getCulledBlockEntities();

                if (blockEntities == null) {
                    continue;
                }

                for (BlockEntity blockEntity : blockEntities) {
                    renderBlockEntity(blockBreakingProgressions, tickDelta, x, y, z, blockEntityRenderer, blockEntity, player);
                }
            }
        }
    }

    private void renderGlobalBlockEntities(Map<Integer, BlockBreakingInfo> blockBreakingProgressions,
                                           float tickDelta,
                                           double x,
                                           double y,
                                           double z,
                                           BlockEntityRenderDispatcher blockEntityRenderer,
                                           ClientPlayerEntity player) {
        for (var renderSection : this.renderSectionManager.getSectionsWithGlobalEntities()) {
            var blockEntities = renderSection.getGlobalBlockEntities();

            if (blockEntities == null) {
                continue;
            }

            for (var blockEntity : blockEntities) {
                renderBlockEntity(blockBreakingProgressions, tickDelta, x, y, z, blockEntityRenderer, blockEntity, player);
            }
        }
    }

    private static void renderBlockEntity(Map<Integer, BlockBreakingInfo> blockBreakingProgressions,
                                          float tickDelta,
                                          double x,
                                          double y,
                                          double z,
                                          BlockEntityRenderDispatcher dispatcher,
                                          BlockEntity entity,
                                          ClientPlayerEntity player) {
        BlockPos pos = entity.getPos();

        GL11.glPushMatrix();
        GL11.glTranslated((double) pos.getX() - x, (double) pos.getY() - y, (double) pos.getZ() - z);

        int destroyProgress = blockBreakingProgressions.values().stream().filter(info ->
            info.getPos().equals(pos)).map((info) -> info.getStage()).findFirst().orElse(-1);


        dispatcher.renderEntity(entity, tickDelta, destroyProgress);


        GL11.glPopMatrix();
    }

    public void iterateVisibleBlockEntities(Consumer<BlockEntity> blockEntityConsumer) {
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
                var renderSection = renderRegion.getSection(renderSectionId);

                var blockEntities = renderSection.getCulledBlockEntities();

                if (blockEntities == null) {
                    continue;
                }

                for (BlockEntity blockEntity : blockEntities) {
                    blockEntityConsumer.accept(blockEntity);
                }
            }
        }

        for (var renderSection : this.renderSectionManager.getSectionsWithGlobalEntities()) {
            var blockEntities = renderSection.getGlobalBlockEntities();

            if (blockEntities == null) {
                continue;
            }

            for (BlockEntity blockEntity : blockEntities) {
                blockEntityConsumer.accept(blockEntity);
            }
        }
    }

    // the volume of a section multiplied by the number of sections to be checked at most
    private static final double MAX_ENTITY_CHECK_VOLUME = 16 * 16 * 16 * 15;

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
            // TODO: do a frustum check instead, even large entities aren't visible if they're outside the frustum
            return true;
        }

        return this.isBoxVisible(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
    }

    public boolean isBoxVisible(double x1, double y1, double z1, double x2, double y2, double z2) {
        // Boxes outside the valid level height will never map to a rendered chunk
        // Always render these boxes, or they'll be culled incorrectly!
        if (y2 < 0 + 0.5D || y1 > 256 - 0.5D) {
            return true;
        }

        int minX = SectionPos.posToSectionCoord(x1 - 0.5D);
        int minY = SectionPos.posToSectionCoord(y1 - 0.5D);
        int minZ = SectionPos.posToSectionCoord(z1 - 0.5D);

        int maxX = SectionPos.posToSectionCoord(x2 + 0.5D);
        int maxY = SectionPos.posToSectionCoord(y2 + 0.5D);
        int maxZ = SectionPos.posToSectionCoord(z2 + 0.5D);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    if (this.renderSectionManager.isSectionVisible(x, y, z)) {
                        return true;
                    }
                }
            }
        }

        return false;
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
