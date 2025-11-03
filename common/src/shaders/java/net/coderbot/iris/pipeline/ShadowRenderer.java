package net.coderbot.iris.pipeline;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gui.option.IrisVideoSettings;
import net.coderbot.iris.mixin.LevelRendererAccessor;
import net.coderbot.iris.shaderpack.OptionalBoolean;
import net.coderbot.iris.shaderpack.PackDirectives;
import net.coderbot.iris.shaderpack.PackShadowDirectives;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shadow.ShadowMatrices;
import net.coderbot.iris.shadows.CullingDataCache;
import net.coderbot.iris.shadows.ShadowRenderTargets;
import net.coderbot.iris.shadows.frustum.BoxCuller;
import net.coderbot.iris.shadows.frustum.CullEverythingFrustum;
import net.coderbot.iris.shadows.frustum.FrustumHolder;
import net.coderbot.iris.shadows.frustum.advanced.AdvancedShadowCullingFrustum;
import net.coderbot.iris.shadows.frustum.fallback.BoxCullingFrustum;
import net.coderbot.iris.shadows.frustum.fallback.NonCullingFrustum;
import net.coderbot.iris.uniforms.CameraUniforms;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.CelestialUniforms;
import net.coderbot.iris.vendored.joml.Matrix4f;
import net.coderbot.iris.vendored.joml.Vector3d;
import net.coderbot.iris.vendored.joml.Vector4f;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ShadowRenderer {
    public static Matrix4f MODELVIEW;
    public static final FloatBuffer MODELVIEW_BUFFER = BufferUtils.createFloatBuffer(16);
    public static Matrix4f PROJECTION;
    public static List<BlockEntity> visibleBlockEntities;
    public static boolean ACTIVE = false;
    private final float halfPlaneLength;
    private final float renderDistanceMultiplier;
    private final float entityShadowDistanceMultiplier;
    private final int resolution;
    private final float intervalSize;
    private final Float fov;
    private final ShadowRenderTargets targets;
    private final OptionalBoolean packCullingState;
    private boolean packHasVoxelization;
    private final boolean shouldRenderTerrain;
    private final boolean shouldRenderTranslucent;
    private final boolean shouldRenderEntities;
    private final boolean shouldRenderPlayer;
    private final boolean shouldRenderBlockEntities;
    private final float sunPathRotation;
    private final List<MipmapPass> mipmapPasses = new ArrayList<>();
    private final String debugStringOverall;
    private FrustumHolder terrainFrustumHolder;
    private FrustumHolder entityFrustumHolder;
    private String debugStringTerrain = "(unavailable)";
    private int renderedShadowEntities = 0;
    private int renderedShadowBlockEntities = 0;
    private Profiler profiler;

    public ShadowRenderer(ProgramSource shadow, PackDirectives directives,
                          ShadowRenderTargets shadowRenderTargets) {

        this.profiler = MinecraftClient.getInstance().profiler;

        final PackShadowDirectives shadowDirectives = directives.getShadowDirectives();

        this.halfPlaneLength = shadowDirectives.getDistance();
        this.renderDistanceMultiplier = shadowDirectives.getDistanceRenderMul();
        this.entityShadowDistanceMultiplier = shadowDirectives.getEntityShadowDistanceMul();
        this.resolution = shadowDirectives.getResolution();
        this.intervalSize = shadowDirectives.getIntervalSize();
        this.shouldRenderTerrain = shadowDirectives.shouldRenderTerrain();
        this.shouldRenderTranslucent = shadowDirectives.shouldRenderTranslucent();
        this.shouldRenderEntities = shadowDirectives.shouldRenderEntities();
        this.shouldRenderPlayer = shadowDirectives.shouldRenderPlayer();
        this.shouldRenderBlockEntities = shadowDirectives.shouldRenderBlockEntities();

        debugStringOverall = "half plane = " + halfPlaneLength + " meters @ " + resolution + "x" + resolution;

        this.terrainFrustumHolder = new FrustumHolder();
        this.entityFrustumHolder = new FrustumHolder();

        this.fov = shadowDirectives.getFov();
        this.targets = shadowRenderTargets;

        if (shadow != null) {
            // Assume that the shader pack is doing voxelization if a geometry shader is detected.
            // Also assume voxelization if image load / store is detected.
            this.packHasVoxelization = shadow.getGeometrySource().isPresent();
            this.packCullingState = shadowDirectives.getCullingState();
        } else {
            this.packHasVoxelization = false;
            this.packCullingState = OptionalBoolean.DEFAULT;
        }

        this.sunPathRotation = directives.getSunPathRotation();

        configureSamplingSettings(shadowDirectives);
    }

    public void setUsesImages(boolean usesImages) {
        this.packHasVoxelization = packHasVoxelization || usesImages;
    }

    public static PoseStack createShadowModelView(float sunPathRotation, float intervalSize) {
        // Determine the camera position
        Vector3d cameraPos = CameraUniforms.getUnshiftedCameraPosition();

        double cameraX = cameraPos.x;
        double cameraY = cameraPos.y;
        double cameraZ = cameraPos.z;

        // Set up our modelview matrix stack
        PoseStack modelView = new PoseStack();
        ShadowMatrices.createModelViewMatrix(modelView, getShadowAngle(), intervalSize, sunPathRotation, cameraX, cameraY, cameraZ);

        return modelView;
    }

    private static ClientWorld getLevel() {
        return Objects.requireNonNull(MinecraftClient.getInstance().world);
    }

    private static float getSkyAngle() {
        return getLevel().getSkyAngle(CapturedRenderingState.INSTANCE.getTickDelta());
    }

    private static float getSunAngle() {
        float skyAngle = getSkyAngle();

        if (skyAngle < 0.75F) {
            return skyAngle + 0.25F;
        } else {
            return skyAngle - 0.75F;
        }
    }

    private static float getShadowAngle() {
        float shadowAngle = getSunAngle();

        if (!CelestialUniforms.isDay()) {
            shadowAngle -= 0.5F;
        }

        return shadowAngle;
    }

    private void configureSamplingSettings(PackShadowDirectives shadowDirectives) {
        final ImmutableList<PackShadowDirectives.DepthSamplingSettings> depthSamplingSettings =
                shadowDirectives.getDepthSamplingSettings();

        final ImmutableList<PackShadowDirectives.SamplingSettings> colorSamplingSettings =
                shadowDirectives.getColorSamplingSettings();

        RenderSystem.activeTexture(GL20C.GL_TEXTURE4);

        configureDepthSampler(targets.getDepthTexture().getTextureId(), depthSamplingSettings.get(0));

        configureDepthSampler(targets.getDepthTextureNoTranslucents().getTextureId(), depthSamplingSettings.get(1));

        for (int i = 0; i < colorSamplingSettings.size(); i++) {
            int glTextureId = targets.get(i).getMainTexture();

            configureSampler(glTextureId, colorSamplingSettings.get(i));
        }

        RenderSystem.activeTexture(GL20C.GL_TEXTURE0);
    }

    private void configureDepthSampler(int glTextureId, PackShadowDirectives.DepthSamplingSettings settings) {
        if (settings.getHardwareFiltering()) {
            // We have to do this or else shadow hardware filtering breaks entirely!
            IrisRenderSystem.texParameteri(glTextureId, GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_COMPARE_MODE, GL30C.GL_COMPARE_REF_TO_TEXTURE);
        }

        // Workaround for issues with old shader packs like Chocapic v4.
        // They expected the driver to put the depth value in z, but it's supposed to only
        // be available in r. So we set up the swizzle to fix that.
        IrisRenderSystem.texParameteriv(glTextureId, GL20C.GL_TEXTURE_2D, ARBTextureSwizzle.GL_TEXTURE_SWIZZLE_RGBA,
                new int[] { GL30C.GL_RED, GL30C.GL_RED, GL30C.GL_RED, GL30C.GL_ONE });

        configureSampler(glTextureId, settings);
    }

    private void configureSampler(int glTextureId, PackShadowDirectives.SamplingSettings settings) {
        if (settings.getMipmap()) {
            int filteringMode = settings.getNearest() ? GL20C.GL_NEAREST_MIPMAP_NEAREST : GL20C.GL_LINEAR_MIPMAP_LINEAR;
            mipmapPasses.add(new MipmapPass(glTextureId, filteringMode));
        }

        if (!settings.getNearest()) {
            // Make sure that things are smoothed
            IrisRenderSystem.texParameteri(glTextureId, GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_LINEAR);
            IrisRenderSystem.texParameteri(glTextureId, GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MAG_FILTER, GL20C.GL_LINEAR);
        } else {
            IrisRenderSystem.texParameteri(glTextureId, GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_NEAREST);
            IrisRenderSystem.texParameteri(glTextureId, GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MAG_FILTER, GL20C.GL_NEAREST);
        }
    }

    private void generateMipmaps() {
        RenderSystem.activeTexture(GL20C.GL_TEXTURE4);

        for (MipmapPass mipmapPass : mipmapPasses) {
            setupMipmappingForTexture(mipmapPass.getTexture(), mipmapPass.getTargetFilteringMode());
        }

        RenderSystem.activeTexture(GL20C.GL_TEXTURE0);
    }

    private void setupMipmappingForTexture(int texture, int filteringMode) {
        IrisRenderSystem.generateMipmaps(texture, GL20C.GL_TEXTURE_2D);
        IrisRenderSystem.texParameteri(texture, GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, filteringMode);
    }

    private FrustumHolder createShadowFrustum(float renderMultiplier, FrustumHolder holder) {
        // TODO: Cull entities / block entities with Advanced Frustum Culling even if voxelization is detected.
        String distanceInfo;
        String cullingInfo;
        if ((packCullingState == OptionalBoolean.FALSE || packHasVoxelization) && packCullingState != OptionalBoolean.TRUE) {
            double distance = halfPlaneLength * renderMultiplier;

            String reason;

            if (packCullingState == OptionalBoolean.FALSE) {
                reason = "(set by shader pack)";
            } else /*if (packHasVoxelization)*/ {
                reason = "(voxelization detected)";
            }

            if (distance <= 0 || distance > MinecraftClient.getInstance().options.viewDistance * 16) {
                distanceInfo = MinecraftClient.getInstance().options.viewDistance * 16
                        + " blocks (capped by normal render distance)";
                cullingInfo = "disabled " + reason;
                return holder.setInfo(new NonCullingFrustum(), distanceInfo, cullingInfo);
            } else {
                distanceInfo = distance + " blocks (set by shader pack)";
                cullingInfo = "distance only " + reason;
                BoxCuller boxCuller = new BoxCuller(distance);
                holder.setInfo(new BoxCullingFrustum(boxCuller), distanceInfo, cullingInfo);
            }
        } else {
            BoxCuller boxCuller;

            double distance = halfPlaneLength * renderMultiplier;
            String setter = "(set by shader pack)";

            if (renderMultiplier < 0) {
                distance = IrisVideoSettings.shadowDistance * 16;
                setter = "(set by user)";
            }

            if (distance >= MinecraftClient.getInstance().options.viewDistance * 16) {
                distanceInfo = MinecraftClient.getInstance().options.viewDistance * 16
                        + " blocks (capped by normal render distance)";
                boxCuller = null;
            } else {
                distanceInfo = distance + " blocks " + setter;

                if (distance == 0.0) {
                    cullingInfo = "no shadows rendered";
                    holder.setInfo(new CullEverythingFrustum(), distanceInfo, cullingInfo);
                }

                boxCuller = new BoxCuller(distance);
            }

            cullingInfo = "Advanced Frustum Culling enabled";

            Vector4f shadowLightPosition = new CelestialUniforms(sunPathRotation).getShadowLightPositionInWorldSpace();

            net.coderbot.iris.vendored.joml.Vector3f shadowLightVectorFromOrigin =
                    new net.coderbot.iris.vendored.joml.Vector3f(shadowLightPosition.x(), shadowLightPosition.y(), shadowLightPosition.z());

            shadowLightVectorFromOrigin.normalize();

            return holder.setInfo(new AdvancedShadowCullingFrustum(CapturedRenderingState.INSTANCE.getGbufferModelView(),
                    CapturedRenderingState.INSTANCE.getGbufferProjection(), shadowLightVectorFromOrigin, boxCuller), distanceInfo, cullingInfo);

        }

        return holder;
    }

    private void setupGlState(Matrix4f projMatrix) {
        // Set up our projection matrix and load it into the legacy matrix stack
        IrisRenderSystem.setupProjectionMatrix(projMatrix.get(new float[16]));

        // Disable backface culling
        // This partially works around an issue where if the front face of a mountain isn't visible, it casts no
        // shadow.
        //
        // However, it only partially resolves issues of light leaking into caves.
        //
        // TODO: Better way of preventing light from leaking into places where it shouldn't
        RenderSystem.disableCull();
    }

    private void restoreGlState() {
        // Restore backface culling
        RenderSystem.enableCull();

        // Make sure to unload the projection matrix
        IrisRenderSystem.restoreProjectionMatrix();
    }

    private void copyPreTranslucentDepth() {
        profiler.swap("translucent depth copy");

        targets.copyPreTranslucentDepth();
    }

    private void renderEntities(LevelRendererAccessor levelRenderer, CameraView frustum, PoseStack modelView, double cameraX, double cameraY, double cameraZ, float tickDelta) {
        EntityRenderDispatcher dispatcher = levelRenderer.getEntityRenderDispatcher();

        AtomicInteger shadowEntities = new AtomicInteger();

        profiler.push("cull");

        dispatcher.updateCamera(cameraX, cameraY, cameraZ);

        List<Entity> renderedEntities = new ArrayList<>(32);

        // TODO: I'm sure that this can be improved / optimized.
        for (Entity entity : getLevel().loadedEntities) {
            if (!dispatcher.shouldRender(entity, frustum, cameraX, cameraY, cameraZ)) {
                continue;
            }

            renderedEntities.add(entity);
        }

        profiler.swap("build geometry");

//        modelView.with(() -> {
            for (Entity entity : renderedEntities) {
                dispatcher.renderEntity(entity, tickDelta);
                shadowEntities.getAndIncrement();
            }
//        });

        renderedShadowEntities = shadowEntities.get();

        profiler.pop();
    }

    private void renderPlayerEntity(LevelRendererAccessor levelRenderer, CameraView frustum, PoseStack modelView, double cameraX, double cameraY, double cameraZ, float tickDelta) {
        EntityRenderDispatcher dispatcher = levelRenderer.getEntityRenderDispatcher();

        profiler.push("cull");

        Entity player = MinecraftClient.getInstance().player;

        if (!dispatcher.shouldRender(player, frustum, cameraX, cameraY, cameraZ)) {
            return;
        }

        profiler.swap("build geometry");

        AtomicInteger shadowEntities = new AtomicInteger();

//        modelView.with(() -> {
            if (player.rider != null) {
                dispatcher.renderEntity(player.rider, tickDelta);
                shadowEntities.getAndIncrement();
            }

            if (player.vehicle != null) {
                dispatcher.renderEntity(player.vehicle, tickDelta);
                shadowEntities.getAndIncrement();
            }

            dispatcher.renderEntity(player, tickDelta);
//        });

        shadowEntities.getAndIncrement();

        renderedShadowEntities = shadowEntities.get();

        profiler.pop();
    }

    private void renderBlockEntities(PoseStack modelView, double cameraX, double cameraY, double cameraZ, float tickDelta, boolean hasEntityFrustum) {
        profiler.push("build blockentities");

        int shadowBlockEntities = 0;
        BoxCuller culler = null;
        if (hasEntityFrustum) {
            culler = new BoxCuller(halfPlaneLength * (renderDistanceMultiplier * entityShadowDistanceMultiplier));
            culler.setPosition(cameraX, cameraY, cameraZ);
        }

        for (BlockEntity entity : visibleBlockEntities) {
            BlockPos pos = entity.getPos();
            if (hasEntityFrustum) {
                if (culler.isCulled(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)) {
                    continue;
                }
            }

            modelView.pushPose();
            modelView.translate(pos.getX() - cameraX, pos.getY() - cameraY, pos.getZ() - cameraZ);
//            modelView.with(() ->
                    BlockEntityRenderDispatcher.INSTANCE.renderEntity(entity, tickDelta, -1);
//        );
            modelView.popPose();

            shadowBlockEntities++;
        }

        renderedShadowBlockEntities = shadowBlockEntities;

        profiler.pop();
    }

    public void renderShadows(LevelRendererAccessor levelRenderer) {
        // We have to re-query this each frame since this changes based on whether the profiler is active
        // If the profiler is inactive, it will return InactiveProfiler.INSTANCE
        this.profiler = MinecraftClient.getInstance().profiler;

        // Get the current tick delta. Normally this is the same as client.getTickDelta(), but when the game is paused,
        // it is set to a fixed value.
        final float tickDelta = CapturedRenderingState.INSTANCE.getTickDelta();

        MinecraftClient client = MinecraftClient.getInstance();

        profiler.swap("shadows");
        ACTIVE = true;

        visibleBlockEntities = new ArrayList<>();

        // Create our camera
        PoseStack modelView = createShadowModelView(this.sunPathRotation, this.intervalSize);
        MODELVIEW = new Matrix4f(modelView.last().pose());
        if (this.fov != null) {
            // If FOV is not null, the pack wants a perspective based projection matrix. (This is to support legacy packs)
            PROJECTION = ShadowMatrices.createPerspectiveMatrix(this.fov);
        } else {
            PROJECTION = ShadowMatrices.createOrthoMatrix(halfPlaneLength);
        }

        profiler.push("terrain_setup");

        if (levelRenderer instanceof CullingDataCache) {
            ((CullingDataCache) levelRenderer).saveState();
        }

        profiler.push("initialize frustum");

        terrainFrustumHolder = createShadowFrustum(renderDistanceMultiplier, terrainFrustumHolder);

        // Determine the player camera position
        Vector3d cameraPos = CameraUniforms.getUnshiftedCameraPosition();

        double cameraX = cameraPos.x();
        double cameraY = cameraPos.y();
        double cameraZ = cameraPos.z();

        // Center the frustum on the player camera position
        terrainFrustumHolder.getFrustum().start();

        profiler.pop();

        // Disable chunk occlusion culling - it's a bit complex to get this properly working with shadow rendering
        // as-is, however in the future it will be good to work on restoring it for a nice performance boost.
        //
        // TODO: Get chunk occlusion working with shadows
        boolean wasChunkCullingEnabled = client.chunkCullingEnabled;
        client.chunkCullingEnabled = false;

        // Always schedule a terrain update
        // TODO: Only schedule a terrain update if the sun / moon is moving, or the shadow map camera moved.
        // We have to ensure that we don't regenerate clouds every frame, since that's what needsUpdate ends up doing.
        // This took up to 10% of the frame time before we applied this fix! That's really bad!
        // Execute the vanilla terrain setup / culling routines using our shadow frustum.
        var cameraView = new CullingCameraView(terrainFrustumHolder.getFrustum());

        levelRenderer.invokeSetupRender(client.player, tickDelta, cameraView, levelRenderer.getFrameId(), false);

        // Don't forget to increment the frame counter! This variable is arbitrary and only used in terrain setup,
        // and if it's not incremented, the vanilla culling code will get confused and think that it's already seen
        // chunks during traversal, and break rendering in concerning ways.
        levelRenderer.setFrameId(levelRenderer.getFrameId() + 1);

        client.chunkCullingEnabled = wasChunkCullingEnabled;

        profiler.swap("terrain");

        setupGlState(PROJECTION);

        // Render all opaque terrain unless pack requests not to
        if (shouldRenderTerrain) {
            levelRenderer.invokeRenderLayer(RenderLayer.SOLID, tickDelta, 2, client.player);
            levelRenderer.invokeRenderLayer(RenderLayer.CUTOUT, tickDelta, 2, client.player);
            levelRenderer.invokeRenderLayer(RenderLayer.CUTOUT_MIPPED, tickDelta, 2, client.player);
        }

        profiler.swap("entities");

        // Create a constrained shadow frustum for entities to avoid rendering faraway entities in the shadow pass,
        // if the shader pack has requested it. Otherwise, use the same frustum as for terrain.
        boolean hasEntityFrustum = false;

        if (entityShadowDistanceMultiplier == 1.0F || entityShadowDistanceMultiplier < 0.0F) {
            entityFrustumHolder.setInfo(terrainFrustumHolder.getFrustum(), terrainFrustumHolder.getDistanceInfo(), terrainFrustumHolder.getCullingInfo());
        } else {
            hasEntityFrustum = true;
            entityFrustumHolder = createShadowFrustum(renderDistanceMultiplier * entityShadowDistanceMultiplier, entityFrustumHolder);
        }

        Frustum entityShadowFrustum = entityFrustumHolder.getFrustum();
        entityShadowFrustum.start();

        cameraView = new CullingCameraView(entityFrustumHolder.getFrustum());

        if (shouldRenderEntities) {
            renderEntities(levelRenderer, cameraView, modelView, cameraX, cameraY, cameraZ, tickDelta);
        } else if (shouldRenderPlayer) {
            renderPlayerEntity(levelRenderer, cameraView, modelView, cameraX, cameraY, cameraZ, tickDelta);
        }

        if (shouldRenderBlockEntities) {
            renderBlockEntities(modelView, cameraX, cameraY, cameraZ, tickDelta, hasEntityFrustum);
        }

        profiler.swap("draw entities");

        copyPreTranslucentDepth();

        profiler.swap("translucent terrain");

        // TODO: Prevent these calls from scheduling translucent sorting...
        // It doesn't matter a ton, since this just means that they won't be sorted in the normal rendering pass.
        // Just something to watch out for, however...
        if (shouldRenderTranslucent) {
            levelRenderer.invokeRenderLayer(RenderLayer.TRANSLUCENT, tickDelta, 2, client.player);
        }

        debugStringTerrain = ((WorldRenderer)levelRenderer).getChunksDebugString();

        profiler.swap("generate mipmaps");

        generateMipmaps();

        profiler.swap("restore gl state");

        restoreGlState();

        if (levelRenderer instanceof CullingDataCache) {
            ((CullingDataCache) levelRenderer).restoreState();
        }

        ACTIVE = false;
        profiler.pop();
        profiler.swap("updatechunks");
    }

    public void addDebugText(List<String> messages) {
        messages.add("[" + Iris.MODNAME + "] Shadow Maps: " + debugStringOverall);
        messages.add("[" + Iris.MODNAME + "] Shadow Distance Terrain: " + terrainFrustumHolder.getDistanceInfo() + " Entity: " + entityFrustumHolder.getDistanceInfo());
        messages.add("[" + Iris.MODNAME + "] Shadow Culling Terrain: " + terrainFrustumHolder.getCullingInfo() + " Entity: " + entityFrustumHolder.getCullingInfo());
        messages.add("[" + Iris.MODNAME + "] Shadow Terrain: " + debugStringTerrain
                + (shouldRenderTerrain ? "" : " (no terrain) ") + (shouldRenderTranslucent ? "" : "(no translucent)"));
        messages.add("[" + Iris.MODNAME + "] Shadow Entities: " + getEntitiesDebugString());
        messages.add("[" + Iris.MODNAME + "] Shadow Block Entities: " + getBlockEntitiesDebugString());
    }

    private String getEntitiesDebugString() {
        return (shouldRenderEntities || shouldRenderPlayer) ? (renderedShadowEntities + "/" + MinecraftClient.getInstance().world.loadedEntities.size()) : "disabled by pack";
    }

    private String getBlockEntitiesDebugString() {
        return shouldRenderBlockEntities ? (renderedShadowBlockEntities + "/" + MinecraftClient.getInstance().world.blockEntities.size()) : "disabled by pack";
    }

    private static class MipmapPass {
        private final int texture;
        private final int targetFilteringMode;

        public MipmapPass(int texture, int targetFilteringMode) {
            this.texture = texture;
            this.targetFilteringMode = targetFilteringMode;
        }

        public int getTexture() {
            return texture;
        }

        public int getTargetFilteringMode() {
            return targetFilteringMode;
        }
    }
}