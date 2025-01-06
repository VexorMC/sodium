package net.coderbot.iris.pipeline;

import com.google.common.collect.ImmutableList;

import java.lang.Math;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.mixin.GameRendererAccessor;
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
import net.coderbot.iris.vendored.joml.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.profiler.Profiler;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBTextureSwizzle;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

public class ShadowRenderer {
    public static final Matrix4f MODELVIEW = new Matrix4f();
    public static final FloatBuffer MODELVIEW_BUFFER = BufferUtils.createFloatBuffer(16);
    public static final Matrix4f PROJECTION = new Matrix4f();
    public static List<BlockEntity> visibleTileEntities;
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
    //	private final RenderBuffers buffers;
//	private final RenderBuffersExt renderBuffersExt;
    private final List<MipmapPass> mipmapPasses = new ArrayList<>();
    private final String debugStringOverall;
    private FrustumHolder terrainFrustumHolder;
    private FrustumHolder entityFrustumHolder;
    private String debugStringTerrain = "(unavailable)";
    private int renderedShadowEntities = 0;
    private int renderedShadowTileEntities = 0;
    private Profiler profiler;

    public ShadowRenderer(ProgramSource shadow, PackDirectives directives, ShadowRenderTargets shadowRenderTargets) {

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

//		this.buffers = new RenderBuffers();
//
//		if (this.buffers instanceof RenderBuffersExt) {
//			this.renderBuffersExt = (RenderBuffersExt) buffers;
//		} else {
//			this.renderBuffersExt = null;
//		}

        configureSamplingSettings(shadowDirectives);
    }

    public void setUsesImages(boolean usesImages) {
        this.packHasVoxelization = packHasVoxelization || usesImages;
    }

    public static PoseStack createShadowModelView(float sunPathRotation, float intervalSize) {
        // Determine the camera position
        final Vector3d cameraPos = CameraUniforms.getUnshiftedCameraPosition();

        final double cameraX = cameraPos.x;
        final double cameraY = cameraPos.y;
        final double cameraZ = cameraPos.z;

        // Set up our modelview matrix stack
        final PoseStack modelView = new PoseStack();
        ShadowMatrices.createModelViewMatrix(modelView, getShadowAngle(), intervalSize, sunPathRotation, cameraX, cameraY, cameraZ);

        return modelView;
    }

    private static ClientWorld getLevel() {
        return Objects.requireNonNull(MinecraftClient.getInstance().world);
    }

    private static float getSkyAngle() {
        return MinecraftClient.getInstance().world.getSkyAngle(CapturedRenderingState.INSTANCE.getTickDelta());
    }

    private static float getSunAngle() {
        final float skyAngle = getSkyAngle();

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

        GlStateManager.activeTexture(GL13.GL_TEXTURE4);

        configureDepthSampler(targets.getDepthTexture().getTextureId(), depthSamplingSettings.get(0));

        configureDepthSampler(targets.getDepthTextureNoTranslucents().getTextureId(), depthSamplingSettings.get(1));

        for (int i = 0; i < colorSamplingSettings.size(); i++) {
            int glTextureId = targets.get(i).getMainTexture();

            configureSampler(glTextureId, colorSamplingSettings.get(i));
        }

        GlStateManager.activeTexture(GL13.GL_TEXTURE0);
    }

    private void configureDepthSampler(int glTextureId, PackShadowDirectives.DepthSamplingSettings settings) {
        if (settings.getHardwareFiltering()) {
            // We have to do this or else shadow hardware filtering breaks entirely!
            IrisRenderSystem.texParameteri(glTextureId, GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_MODE, GL30.GL_COMPARE_REF_TO_TEXTURE);
        }

        // Workaround for issues with old shader packs like Chocapic v4.
        // They expected the driver to put the depth value in z, but it's supposed to only
        // be available in r. So we set up the swizzle to fix that.
        IrisRenderSystem.texParameteriv(glTextureId, GL11.GL_TEXTURE_2D, ARBTextureSwizzle.GL_TEXTURE_SWIZZLE_RGBA, new int[] { GL11.GL_RED, GL11.GL_RED, GL11.GL_RED, GL11.GL_ONE });

        configureSampler(glTextureId, settings);
    }

    private void configureSampler(int glTextureId, PackShadowDirectives.SamplingSettings settings) {
        if (settings.getMipmap()) {
            final int filteringMode = settings.getNearest() ? GL11.GL_NEAREST_MIPMAP_NEAREST : GL11.GL_LINEAR_MIPMAP_LINEAR;
            mipmapPasses.add(new MipmapPass(glTextureId, filteringMode));
        }

        if (!settings.getNearest()) {
            // Make sure that things are smoothed
            IrisRenderSystem.texParameteri(glTextureId, GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            IrisRenderSystem.texParameteri(glTextureId, GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        } else {
            IrisRenderSystem.texParameteri(glTextureId, GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            IrisRenderSystem.texParameteri(glTextureId, GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }
    }

    private void generateMipmaps() {
        RenderSystem.activeTexture(GL13.GL_TEXTURE4);

        for (MipmapPass mipmapPass : mipmapPasses) {
            setupMipmappingForTexture(mipmapPass.getTexture(), mipmapPass.getTargetFilteringMode());
        }

        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
    }

    private void setupMipmappingForTexture(int texture, int filteringMode) {
        IrisRenderSystem.generateMipmaps(texture, GL11.GL_TEXTURE_2D);
        IrisRenderSystem.texParameteri(texture, GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filteringMode);
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
                // TODO: GUI
//				distance = IrisVideoSettings.shadowDistance * 16;
                distance = 32 * 16;
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

            Vector3f shadowLightVectorFromOrigin =
                    new Vector3f(shadowLightPosition.x(), shadowLightPosition.y(), shadowLightPosition.z());

            shadowLightVectorFromOrigin.normalize();

            return holder.setInfo(new AdvancedShadowCullingFrustum(new Matrix4f(Camera.PROJECTION_MATRIX), new Matrix4f(Camera.PROJECTION_MATRIX),
                    shadowLightVectorFromOrigin, boxCuller), distanceInfo, cullingInfo);

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

    private void renderEntities(WorldRenderer levelRenderer, Frustum frustum, PoseStack modelView, double cameraX, double cameraY, double cameraZ, float tickDelta) {
        int shadowEntities = 0;

        profiler.push("cull");

        List<Entity> renderedEntities = new ArrayList<>(32);

        // TODO: I'm sure that this can be improved / optimized.
        // TODO: Entity culling
        for (Entity entity : getLevel().getLoadedEntities()) {
            if (false/*!dispatcher.shouldRender(entity, frustum, cameraX, cameraY, cameraZ) || entity.isSpectator()*/) {
                continue;
            }

            renderedEntities.add(entity);
        }

        profiler.swap("sort");

        // TODO: Render
        // Sort the entities by type first in order to allow vanilla's entity batching system to work better.
        renderedEntities.sort(Comparator.comparingInt(entity -> entity.getClass().hashCode()));

        profiler.swap("build geometry");

        // TODO: Render
        GL11.glPushMatrix();
        MODELVIEW_BUFFER.clear().rewind();
        modelView.last().pose().get(MODELVIEW_BUFFER);
        GL11.glLoadMatrixf(MODELVIEW_BUFFER);
        for (Entity entity : renderedEntities) {
            MinecraftClient.getInstance().getEntityRenderManager().renderEntity(entity, tickDelta);
            shadowEntities++;
        }
        GL11.glPopMatrix();

        renderedShadowEntities = shadowEntities;

        profiler.pop();
    }

    private void renderPlayerEntity(WorldRenderer levelRenderer, Frustum frustum, PoseStack modelView, double cameraX, double cameraY, double cameraZ, float tickDelta) {
        profiler.push("cull");

        Entity player = MinecraftClient.getInstance().player;

        // TODO: Entity culling
//		if (!dispatcher.shouldRender(player, frustum, cameraX, cameraY, cameraZ) || player.isSpectator()) {
//			return;
//		}

        profiler.swap("build geometry");

        int shadowEntities = 0;

        GL11.glPushMatrix();
        MODELVIEW_BUFFER.clear().rewind();
        modelView.last().pose().get(MODELVIEW_BUFFER);
        GL11.glLoadMatrixf(MODELVIEW_BUFFER);

        if (player.rider != null) {
            MinecraftClient.getInstance().getEntityRenderManager().renderEntity(player.rider, tickDelta);
            shadowEntities++;
        }

        if (player.vehicle != null) {
            MinecraftClient.getInstance().getEntityRenderManager().renderEntity(player.vehicle, tickDelta);
            shadowEntities++;
        }

        MinecraftClient.getInstance().getEntityRenderManager().renderEntity(player, tickDelta);

        GL11.glPopMatrix();

        shadowEntities++;

        renderedShadowEntities = shadowEntities;

        profiler.pop();
    }

    private void renderTileEntity(BlockEntity tile, double cameraX, double cameraY, double cameraZ, float partialTicks) {
        if (tile.getSquaredDistance(cameraX, cameraY, cameraZ) >= tile.getSquaredRenderDistance()) {
            return;
        }
        int brightness = tile.getEntityWorld().getLight(tile.getPos(), 0);
        GLX.gl13MultiTexCoord2f(GLX.lightmapTextureUnit, (float) brightness % 65536, (float) brightness / 65536);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        BlockEntityRenderDispatcher.INSTANCE.renderEntity(tile,
                (double)tile.getPos().getX() - cameraX,
                (double)tile.getPos().getY() - cameraY,
                (double)tile.getPos().getZ() - cameraZ,
                partialTicks,
                -1
        );
    }

    private void renderTileEntities(Object bufferSource, PoseStack modelView, double cameraX, double cameraY, double cameraZ, float partialTicks, boolean hasEntityFrustum) {
        profiler.push("build blockentities");

        int shadowTileEntities = 0;
        BoxCuller culler = null;
        if (hasEntityFrustum) {
            culler = new BoxCuller(halfPlaneLength * (renderDistanceMultiplier * entityShadowDistanceMultiplier));
            culler.setPosition(cameraX, cameraY, cameraZ);
        }

        GL11.glPushMatrix();
        MODELVIEW_BUFFER.clear().rewind();
        modelView.last().pose().get(MODELVIEW_BUFFER);
        GL11.glLoadMatrixf(MODELVIEW_BUFFER);
        for (BlockEntity tileEntity : visibleTileEntities) {
            if (hasEntityFrustum && (culler.isCulled(tileEntity.getPos().getX() - 1, tileEntity.getPos().getY()- 1, tileEntity.getPos().getZ() - 1, tileEntity.getPos().getX() + 1, tileEntity.getPos().getY() + 1, tileEntity.getPos().getZ() + 1))) {
                continue;
            }
            renderTileEntity(tileEntity, cameraX, cameraY, cameraZ, partialTicks);

            shadowTileEntities++;
        }
        GlStateManager.popMatrix();

        renderedShadowTileEntities = shadowTileEntities;

        profiler.pop();
    }

    public void renderShadows(LevelRendererAccessor levelRenderer) {
        final MinecraftClient mc = MinecraftClient.getInstance();
        final GameRenderer rg = mc.gameRenderer;

        // We have to re-query this each frame since this changes based on whether the profiler is active
        // If the profiler is inactive, it will return InactiveProfiler.INSTANCE
        this.profiler = MinecraftClient.getInstance().profiler;

        profiler.swap("shadows");
        ACTIVE = true;

        // NB: We store the previous player buffers in order to be able to allow mods rendering entities in the shadow pass (Flywheel) to use the shadow buffers instead.
        // TODO: Render
//		RenderBuffers playerBuffers = levelRenderer.getRenderBuffers();
//		levelRenderer.setRenderBuffers(buffers);

        visibleTileEntities = new ArrayList<>();

        // Create our camera
        final PoseStack modelView = createShadowModelView(this.sunPathRotation, this.intervalSize);
        MODELVIEW.set(modelView.last().pose());

        final Matrix4f shadowProjection;
        if (this.fov != null) {
            // If FOV is not null, the pack wants a perspective based projection matrix. (This is to support legacy packs)
            shadowProjection = ShadowMatrices.createPerspectiveMatrix(this.fov);
        } else {
            shadowProjection = ShadowMatrices.createOrthoMatrix(halfPlaneLength);
        }

        PROJECTION.set(shadowProjection);

        profiler.push("terrain_setup");

        if (levelRenderer instanceof CullingDataCache) {
            ((CullingDataCache) levelRenderer).saveState();
        }

        profiler.push("initialize frustum");

        terrainFrustumHolder = createShadowFrustum(renderDistanceMultiplier, terrainFrustumHolder);

        // Determine the player camera position
        final Vector3d cameraPos = CameraUniforms.getUnshiftedCameraPosition();

        final double cameraX = cameraPos.x();
        final double cameraY = cameraPos.y();
        final double cameraZ = cameraPos.z();

        // Center the frustum on the player camera position
        terrainFrustumHolder.getFrustum().start();

        profiler.pop();

        mc.getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
        DiffuseLighting.disable();

        // Always schedule a terrain update
        // TODO: Only schedule a terrain update if the sun / moon is moving, or the shadow map camera moved.
        // We have to ensure that we don't regenerate clouds every frame, since that's what needsUpdate ends up doing.
        // This took up to 10% of the frame time before we applied this fix! That's really bad!
//		boolean regenerateClouds = levelRenderer.shouldRegenerateClouds();
//		((LevelRenderer) levelRenderer).needsUpdate();
//		levelRenderer.setShouldRegenerateClouds(regenerateClouds);

        // Execute the vanilla terrain setup / culling routines using our shadow frustum.
        levelRenderer.invokeSetupRender(MinecraftClient.getInstance().player, CapturedRenderingState.INSTANCE.getTickDelta(), new CullingCameraView(terrainFrustumHolder.getFrustum()), ((GameRendererAccessor)MinecraftClient.getInstance().gameRenderer).getFrameCount(), MinecraftClient.getInstance().player.isSpectator());

        // Don't forget to increment the frame counter! This variable is arbitrary and only used in terrain setup,
        // and if it's not incremented, the vanilla culling code will get confused and think that it's already seen
        // chunks during traversal, and break rendering in concerning ways.
//		levelRenderer.setFrameId(levelRenderer.getFrameId() + 1);

        profiler.swap("terrain");

        setupGlState(PROJECTION);

        // Render all opaque terrain unless pack requests not to
        if (shouldRenderTerrain) {
            levelRenderer.invokeRenderLayer(RenderLayer.SOLID, CapturedRenderingState.INSTANCE.getTickDelta(), 1, MinecraftClient.getInstance().getCameraEntity());
            levelRenderer.invokeRenderLayer(RenderLayer.CUTOUT, CapturedRenderingState.INSTANCE.getTickDelta(), 1, MinecraftClient.getInstance().getCameraEntity());
            levelRenderer.invokeRenderLayer(RenderLayer.CUTOUT_MIPPED, CapturedRenderingState.INSTANCE.getTickDelta(), 1, MinecraftClient.getInstance().getCameraEntity());
        }

        profiler.swap("entities");

        // Get the current tick delta. Normally this is the same as client.getTickDelta(), but when the game is paused,
        // it is set to a fixed value.
        final float tickDelta = CapturedRenderingState.INSTANCE.getTickDelta();

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

        // Render nearby entities

        if (shouldRenderEntities) {
            renderEntities((WorldRenderer) levelRenderer, entityShadowFrustum, modelView, cameraX, cameraY, cameraZ, tickDelta);
        } else if (shouldRenderPlayer) {
            renderPlayerEntity((WorldRenderer) levelRenderer, entityShadowFrustum, modelView, cameraX, cameraY, cameraZ, tickDelta);
        }

        if (shouldRenderBlockEntities) {
            renderTileEntities(null, modelView, cameraX, cameraY, cameraZ, tickDelta, hasEntityFrustum);
        }

        profiler.swap("draw entities");

        // NB: Don't try to draw the translucent parts of entities afterwards. It'll cause problems since some
        // shader packs assume that everything drawn afterwards is actually translucent and should cast a colored
        // shadow...

        copyPreTranslucentDepth();

        profiler.swap("translucent terrain");

        // TODO (Iris): Prevent these calls from scheduling translucent sorting...
        // It doesn't matter a ton, since this just means that they won't be sorted in the getNormal rendering pass.
        // Just something to watch out for, however...
        if (shouldRenderTranslucent) {
            mc.getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
            levelRenderer.invokeRenderLayer(RenderLayer.TRANSLUCENT, CapturedRenderingState.INSTANCE.getTickDelta(), 1, MinecraftClient.getInstance().player);
        }

        // Note: Apparently tripwire isn't rendered in the shadow pass.
        // worldRenderer.invokeRenderType(RenderType.getTripwire(), modelView, cameraX, cameraY, cameraZ);

//		if (renderBuffersExt != null) {
//			renderBuffersExt.endLevelRendering();
//		}

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
        messages.add("[" + Iris.MODNAME + "] Shadow Terrain: " + debugStringTerrain + (shouldRenderTerrain ? "" : " (no terrain) ") + (shouldRenderTranslucent ? "" : "(no translucent)"));
        messages.add("[" + Iris.MODNAME + "] Shadow Entities: " + getEntitiesDebugString());
        messages.add("[" + Iris.MODNAME + "] Shadow Block Entities: " + getTileEntitiesDebugString());

//		if (buffers instanceof DrawCallTrackingRenderBuffers drawCallTracker && (shouldRenderEntities || shouldRenderPlayer)) {
//            messages.add("[" + Iris.MODNAME + "] Shadow Entity Batching: " + BatchingDebugMessageHelper.getDebugMessage(drawCallTracker));
//		}
    }

    private String getEntitiesDebugString() {
        return (shouldRenderEntities || shouldRenderPlayer) ? (renderedShadowEntities + "/" + MinecraftClient.getInstance().world.getLoadedEntities().size()) : "disabled by pack";
    }

    private String getTileEntitiesDebugString() {
        return shouldRenderBlockEntities ? (renderedShadowTileEntities + "/" + MinecraftClient.getInstance().world.blockEntities.size()) : "disabled by pack";
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