package net.irisshaders.iris.shadows;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import dev.vexor.radium.compat.mojang.math.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.irisshaders.batchedentityrendering.impl.BatchingDebugMessageHelper;
import net.irisshaders.batchedentityrendering.impl.DrawCallTrackingRenderBuffers;
import net.irisshaders.batchedentityrendering.impl.FullyBufferedMultiBufferSource;
import net.irisshaders.batchedentityrendering.impl.MemoryTrackingRenderBuffers;
import net.irisshaders.batchedentityrendering.impl.RenderBuffersExt;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.GLDebug;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gui.option.IrisVideoSettings;
import net.irisshaders.iris.mixin.LevelRendererAccessor;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.shaderpack.properties.PackShadowDirectives;
import net.irisshaders.iris.shaderpack.properties.ShadowCullState;
import net.irisshaders.iris.shadows.frustum.BoxCuller;
import net.irisshaders.iris.shadows.frustum.CullEverythingFrustum;
import net.irisshaders.iris.shadows.frustum.FrustumHolder;
import net.irisshaders.iris.shadows.frustum.advanced.AdvancedShadowCullingFrustum;
import net.irisshaders.iris.shadows.frustum.advanced.ReversedAdvancedShadowCullingFrustum;
import net.irisshaders.iris.shadows.frustum.fallback.BoxCullingFrustum;
import net.irisshaders.iris.shadows.frustum.fallback.NonCullingFrustum;
import net.irisshaders.iris.uniforms.CameraUniforms;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.uniforms.CelestialUniforms;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.CullingCameraView;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ShadowRenderer {
	public static boolean ACTIVE = false;
	public static List<BlockEntity> visibleBlockEntities;
	public static int renderDistance;
	public static Matrix4f MODELVIEW;
	public static Matrix4f PROJECTION;
	public static Frustum FRUSTUM;
	private final float halfPlaneLength;
	private final float nearPlane, farPlane;
	private final float voxelDistance;
	private final float renderDistanceMultiplier;
	private final float entityShadowDistanceMultiplier;
	private final int resolution;
	private final float intervalSize;
	private final Float fov;
	private final ShadowRenderTargets targets;
	private final ShadowCullState packCullingState;
	private final ShadowCompositeRenderer compositeRenderer;
	private final boolean shouldRenderTerrain;
	private final boolean shouldRenderTranslucent;
	private final boolean shouldRenderEntities;
	private final boolean shouldRenderPlayer;
	private final boolean shouldRenderBlockEntities;
	private final boolean shouldRenderDH;
	private final float sunPathRotation;
	private final RenderBuffersExt renderBuffersExt;
	private final List<MipmapPass> mipmapPasses = new ArrayList<>();
	private final String debugStringOverall;
	private final boolean separateHardwareSamplers;
	private final boolean shouldRenderLightBlockEntities;
	private final IrisRenderingPipeline pipeline;
	private boolean packHasVoxelization;
	private FrustumHolder terrainFrustumHolder;
	private FrustumHolder entityFrustumHolder;
	private String debugStringTerrain = "(unavailable)";
	private int renderedShadowEntities = 0;
	private int renderedShadowBlockEntities = 0;

	public ShadowRenderer(IrisRenderingPipeline pipeline, ProgramSource shadow, PackDirectives directives,
						  ShadowRenderTargets shadowRenderTargets, ShadowCompositeRenderer compositeRenderer, CustomUniforms customUniforms, boolean separateHardwareSamplers) {

		this.pipeline = pipeline;
		this.separateHardwareSamplers = separateHardwareSamplers;

		final PackShadowDirectives shadowDirectives = directives.getShadowDirectives();

		this.halfPlaneLength = shadowDirectives.getDistance();
		this.nearPlane = shadowDirectives.getNearPlane();
		this.farPlane = shadowDirectives.getFarPlane();

		this.voxelDistance = shadowDirectives.getVoxelDistance();
		this.renderDistanceMultiplier = shadowDirectives.getDistanceRenderMul();
		this.entityShadowDistanceMultiplier = shadowDirectives.getEntityShadowDistanceMul();
		this.resolution = shadowDirectives.getResolution();
		this.intervalSize = shadowDirectives.getIntervalSize();
		this.shouldRenderTerrain = shadowDirectives.shouldRenderTerrain();
		this.shouldRenderTranslucent = shadowDirectives.shouldRenderTranslucent();
		this.shouldRenderEntities = shadowDirectives.shouldRenderEntities();
		this.shouldRenderPlayer = shadowDirectives.shouldRenderPlayer();
		this.shouldRenderBlockEntities = shadowDirectives.shouldRenderBlockEntities();
		this.shouldRenderLightBlockEntities = shadowDirectives.shouldRenderLightBlockEntities();
		this.shouldRenderDH = shadowDirectives.isDhShadowEnabled().orElse(false);

		this.compositeRenderer = compositeRenderer;

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
			this.packCullingState = ShadowCullState.DEFAULT;
		}

		this.sunPathRotation = directives.getSunPathRotation();

		int processors = Runtime.getRuntime().availableProcessors();

		configureSamplingSettings(shadowDirectives);
	}

	public static PoseStack createShadowModelView(float sunPathRotation, float intervalSize, float nearPlane, float farPlane) {
		// Determine the camera position
		Vector3d cameraPos = CameraUniforms.getUnshiftedCameraPosition();

		double cameraX = cameraPos.x;
		double cameraY = cameraPos.y;
		double cameraZ = cameraPos.z;

		// Set up our modelview matrix stack
		PoseStack modelView = new PoseStack();
		ShadowMatrices.createModelViewMatrix(modelView, getShadowAngle(), intervalSize, sunPathRotation, cameraX, cameraY, cameraZ, nearPlane, farPlane);

		return modelView;
	}

	private static ClientWorld getLevel() {
		return Objects.requireNonNull(MinecraftClient.getInstance().world);
	}

	private static float getSkyAngle() {
		return getLevel().getTimeOfDay();
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

	public void setUsesImages(boolean usesImages) {
		this.packHasVoxelization = packHasVoxelization || usesImages;
	}

	private void configureSamplingSettings(PackShadowDirectives shadowDirectives) {
		final ImmutableList<PackShadowDirectives.DepthSamplingSettings> depthSamplingSettings =
			shadowDirectives.getDepthSamplingSettings();

		final Int2ObjectMap<PackShadowDirectives.SamplingSettings> colorSamplingSettings =
			shadowDirectives.getColorSamplingSettings();

		GlStateManager.activeTexture(GL13.GL_TEXTURE4);

		configureDepthSampler(targets.getDepthTexture().getTextureId(), depthSamplingSettings.get(0));

		configureDepthSampler(targets.getDepthTextureNoTranslucents().getTextureId(), depthSamplingSettings.get(1));

		for (int i = 0; i < targets.getNumColorTextures(); i++) {
			if (targets.get(i) != null) {
				int glTextureId = targets.get(i).getMainTexture();

				configureSampler(glTextureId, colorSamplingSettings.computeIfAbsent(i, a -> new PackShadowDirectives.SamplingSettings()));
			}
		}

        GlStateManager.activeTexture(GL13.GL_TEXTURE0);
	}

	private void configureDepthSampler(int glTextureId, PackShadowDirectives.DepthSamplingSettings settings) {
		if (settings.getHardwareFiltering() && !separateHardwareSamplers) {
			// We have to do this or else shadow hardware filtering breaks entirely!
			IrisRenderSystem.texParameteri(glTextureId, GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_MODE, GL30.GL_COMPARE_REF_TO_TEXTURE);
		}

		// Workaround for issues with old shader packs like Chocapic v4.
		// They expected the driver to put the depth value in z, but it's supposed to only
		// be available in r. So we set up the swizzle to fix that.
		IrisRenderSystem.texParameteriv(glTextureId, GL11.GL_TEXTURE_2D, ARBTextureSwizzle.GL_TEXTURE_SWIZZLE_RGBA,
			new int[]{GL11.GL_RED, GL11.GL_RED, GL11.GL_RED, GL11.GL_ONE});

		configureSampler(glTextureId, settings);
	}

	private void configureSampler(int glTextureId, PackShadowDirectives.SamplingSettings settings) {
		if (settings.getMipmap()) {
			int filteringMode = settings.getNearest() ? GL11.GL_NEAREST_MIPMAP_NEAREST : GL11.GL_LINEAR_MIPMAP_LINEAR;
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
		GlStateManager.activeTexture(GL13.GL_TEXTURE4);

		for (MipmapPass mipmapPass : mipmapPasses) {
			setupMipmappingForTexture(mipmapPass.texture(), mipmapPass.targetFilteringMode());
		}

		GlStateManager.activeTexture(GL13.GL_TEXTURE0);
	}

	private void setupMipmappingForTexture(int texture, int filteringMode) {
		IrisRenderSystem.generateMipmaps(texture, GL11.GL_TEXTURE_2D);
		IrisRenderSystem.texParameteri(texture, GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filteringMode);
	}

	private FrustumHolder createShadowFrustum(float renderMultiplier, FrustumHolder holder) {
		// TODO: Cull entities / block entities with Advanced Frustum Culling even if voxelization is detected.
		String distanceInfo;
		String cullingInfo;
		if ((packCullingState == ShadowCullState.DEFAULT && packHasVoxelization) || packCullingState == ShadowCullState.DISTANCE) {
			double distance = halfPlaneLength * renderMultiplier;

			String reason;

			if (packCullingState == ShadowCullState.DISTANCE) {
				reason = "(set by shader pack)";
			} else /*if (packHasVoxelization)*/ {
				reason = "(voxelization detected)";
			}

			if (distance <= 0 || distance > MinecraftClient.getInstance().options.viewDistance * 16) {
				distanceInfo = "render distance = " + MinecraftClient.getInstance().options.viewDistance * 16
					+ " blocks ";
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

			boolean isReversed = packCullingState == ShadowCullState.REVERSED;

			// Assume render multiplier is meant to be 1 if reversed culling is on
			if (isReversed && renderMultiplier < 0) renderMultiplier = 1.0f;

			double distance = (isReversed ? voxelDistance : halfPlaneLength) * renderMultiplier;
			String setter = "(set by shader pack)";

			if (renderMultiplier < 0) {
				distance = IrisVideoSettings.shadowDistance * 16;
				setter = "(set by user)";
			}

			if (distance >= MinecraftClient.getInstance().options.viewDistance * 16 && !isReversed) {
				distanceInfo = "render distance = " + MinecraftClient.getInstance().options.viewDistance * 16
					+ " blocks ";
				boxCuller = null;
			} else {
				distanceInfo = distance + " blocks " + setter;

				if (distance == 0.0 && !isReversed) {
					cullingInfo = "no shadows rendered";
					holder.setInfo(new CullEverythingFrustum(), distanceInfo, cullingInfo);
				}

				boxCuller = new BoxCuller(distance);
			}

			cullingInfo = (isReversed ? "Reversed" : "Advanced") + " Frustum Culling enabled";

			Vector4f shadowLightPosition = new CelestialUniforms(sunPathRotation).getShadowLightPositionInWorldSpace();

			Vector3f shadowLightVectorFromOrigin =
				new Vector3f(shadowLightPosition.x(), shadowLightPosition.y(), shadowLightPosition.z());

			shadowLightVectorFromOrigin.normalize();

			Matrix4f projView = CapturedRenderingState.INSTANCE.getGbufferProjection()
					.mul(CapturedRenderingState.INSTANCE.getGbufferModelView(), new Matrix4f());

			if (isReversed) {
				return holder.setInfo(new ReversedAdvancedShadowCullingFrustum(projView, PROJECTION, shadowLightVectorFromOrigin, boxCuller, new BoxCuller(halfPlaneLength * renderMultiplier)), distanceInfo, cullingInfo);
			} else {
				return holder.setInfo(new AdvancedShadowCullingFrustum(projView, PROJECTION, shadowLightVectorFromOrigin, boxCuller), distanceInfo, cullingInfo);
			}
		}

		return holder;
	}

	public void setupShadowViewport() {
		// Set up the viewport
		GlStateManager.viewport(0, 0, resolution, resolution);
	}

	public void renderShadows(LevelRendererAccessor levelRenderer) {
		if (IrisVideoSettings.getOverriddenShadowDistance(IrisVideoSettings.shadowDistance) == 0) {
			return;
		}

		MinecraftClient client = MinecraftClient.getInstance();

		MinecraftClient.getInstance().profiler.swap("shadows");
		ACTIVE = true;

		renderDistance = (int) ((halfPlaneLength * renderDistanceMultiplier) / 16);

		if (renderDistanceMultiplier < 0) {
			renderDistance = IrisVideoSettings.shadowDistance;
		}


		visibleBlockEntities = new ArrayList<>();

		// NB: We store the previous player buffers in order to be able to allow mods rendering entities in the shadow pass (Flywheel) to use the shadow buffers instead.
		visibleBlockEntities = new ArrayList<>();
		setupShadowViewport();

		// Create our camera
		PoseStack modelView = createShadowModelView(this.sunPathRotation, this.intervalSize, nearPlane, farPlane);
		MODELVIEW = new Matrix4f(modelView.last().pose());

		// Set up our orthographic projection matrix and load it into RenderSystem
		Matrix4f shadowProjection;
		if (this.fov != null) {
			// If FOV is not null, the pack wants a perspective based projection matrix. (This is to support legacy packs)
			shadowProjection = ShadowMatrices.createPerspectiveMatrix(this.fov);
		} else {
			shadowProjection = ShadowMatrices.createOrthoMatrix(halfPlaneLength, Mth.equal(nearPlane, -1.0f) ? -DHCompat.getRenderDistance() * 16 : nearPlane, Mth.equal(farPlane, -1.0f) ? DHCompat.getRenderDistance() * 16 : farPlane);
		}

		IrisRenderSystem.setShadowProjection(shadowProjection);

		PROJECTION = shadowProjection;

		MinecraftClient.getInstance().profiler.push("terrain_setup");

		if (levelRenderer instanceof CullingDataCache) {
			((CullingDataCache) levelRenderer).saveState();
		}

		MinecraftClient.getInstance().profiler.push("initialize frustum");

		terrainFrustumHolder = createShadowFrustum(renderDistanceMultiplier, terrainFrustumHolder);

		FRUSTUM = terrainFrustumHolder.getFrustum();

		// Determine the player camera position
		Vector3d cameraPos = CameraUniforms.getUnshiftedCameraPosition();

		double cameraX = cameraPos.x();
		double cameraY = cameraPos.y();
		double cameraZ = cameraPos.z();

		// Center the frustum on the player camera position
		terrainFrustumHolder.getFrustum().start();

		MinecraftClient.getInstance().profiler.pop();

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
		boolean regenerateClouds = levelRenderer.shouldRegenerateClouds();
		((WorldRenderer) levelRenderer).scheduleTerrainUpdate();
		levelRenderer.setShouldRegenerateClouds(regenerateClouds);

		// Execute the vanilla terrain setup / culling routines using our shadow frustum.
		levelRenderer.invokeSetupRender(MinecraftClient.getInstance().player, CapturedRenderingState.INSTANCE.getTickDelta(), new CullingCameraView(terrainFrustumHolder.getFrustum()), 0, false);

		// Don't forget to increment the frame counter! This variable is arbitrary and only used in terrain setup,
		// and if it's not incremented, the vanilla culling code will get confused and think that it's already seen
		// chunks during traversal, and break rendering in concerning ways.
		//worldRenderer.setFrameId(worldRenderer.getFrameId() + 1);

		client.chunkCullingEnabled = wasChunkCullingEnabled;

		MinecraftClient.getInstance().profiler.swap("terrain");

		// Disable backface culling
		// This partially works around an issue where if the front face of a mountain isn't visible, it casts no
		// shadow.
		//
		// However, it only partially resolves issues of light leaking into caves.
		//
		// TODO: Better way of preventing light from leaking into places where it shouldn't
		GlStateManager.disableCull();

		// Render all opaque terrain unless pack requests not to
		if (shouldRenderTerrain) {
            double delta = CapturedRenderingState.INSTANCE.getTickDelta();
			levelRenderer.invokeRenderSectionLayer(RenderLayer.SOLID, delta, 1, MinecraftClient.getInstance().getCameraEntity());
			levelRenderer.invokeRenderSectionLayer(RenderLayer.CUTOUT, delta, 1, MinecraftClient.getInstance().getCameraEntity());
			levelRenderer.invokeRenderSectionLayer(RenderLayer.CUTOUT_MIPPED, delta, 1, MinecraftClient.getInstance().getCameraEntity());
		}

		// Reset our viewport in case Sodium overrode it
		GlStateManager.viewport(0, 0, resolution, resolution);

		MinecraftClient.getInstance().profiler.swap("entities");

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
		//
		// Note: We must use a separate BuilderBufferStorage object here, or else very weird things will happen during
		// rendering.
		if (renderBuffersExt != null) {
			renderBuffersExt.beginLevelRendering();
		}


		if (shouldRenderEntities) {
			renderedShadowEntities = renderEntities(levelRenderer, dispatcher, bufferSource, modelView, tickDelta, entityShadowFrustum, cameraX, cameraY, cameraZ);
		} else if (shouldRenderPlayer) {
			renderedShadowEntities = renderPlayerEntity(levelRenderer, dispatcher, bufferSource, modelView, tickDelta, entityShadowFrustum, cameraX, cameraY, cameraZ);
		}

		MinecraftClient.getInstance().profiler.popPush("build blockentities");

		if (shouldRenderBlockEntities) {
			renderedShadowBlockEntities = ShadowRenderingState.renderBlockEntities(this, buffers, modelView, playerCamera, cameraX, cameraY, cameraZ, tickDelta, hasEntityFrustum, false);
		} else if (shouldRenderLightBlockEntities) {
			renderedShadowBlockEntities = ShadowRenderingState.renderBlockEntities(this, buffers, modelView, playerCamera, cameraX, cameraY, cameraZ, tickDelta, hasEntityFrustum, true);
		}

		MinecraftClient.getInstance().profiler.popPush("draw entities");

		// NB: Don't try to draw the translucent parts of entities afterwards in the shadow pass. It'll cause problems since some
		// shader packs assume that everything drawn afterwards is actually translucent and should cast a colored
		// shadow...
		if (bufferSource instanceof FullyBufferedMultiBufferSource fullyBufferedMultiBufferSource)
			fullyBufferedMultiBufferSource.readyUp();

		bufferSource.endBatch();

		copyPreTranslucentDepth(levelRenderer);

		MinecraftClient.getInstance().profiler.popPush("translucent terrain");

		// TODO: Prevent these calls from scheduling translucent sorting...
		// It doesn't matter a ton, since this just means that they won't be sorted in the normal rendering pass.
		// Just something to watch out for, however...
		if (shouldRenderTranslucent) {
			levelRenderer.invokeRenderSectionLayer(RenderType.translucent(), cameraX, cameraY, cameraZ, MODELVIEW, shadowProjection);
		}

		// Note: Apparently tripwire isn't rendered in the shadow pass.
		// levelRenderer.invokeRenderChunkLayer(RenderType.tripwire(), modelView, cameraX, cameraY, cameraZ, shadowProjection);

		if (renderBuffersExt != null) {
			renderBuffersExt.endLevelRendering();
		}

		IrisRenderSystem.restorePlayerProjection();

		debugStringTerrain = ((LevelRenderer) levelRenderer).getSectionStatistics();

		MinecraftClient.getInstance().profiler.popPush("generate mipmaps");

		generateMipmaps();

		MinecraftClient.getInstance().profiler.popPush("restore gl state");

		// Restore backface culling
		RenderSystem.enableCull();

		MinecraftClient.getInstance().getMainRenderTarget().bindWrite(false);

		// Restore the old viewport
		RenderSystem.viewport(0, 0, client.getMainRenderTarget().width, client.getMainRenderTarget().height);

		if (levelRenderer instanceof CullingDataCache) {
			((CullingDataCache) levelRenderer).restoreState();
		}

		pipeline.removePhaseIfNeeded();

		GLDebug.pushGroup(901, "shadowcomp");
		compositeRenderer.renderAll();
		GLDebug.popGroup();

		levelRenderer.setRenderBuffers(playerBuffers);

		visibleBlockEntities = null;
		ACTIVE = false;

		MinecraftClient.getInstance().profiler.pop();
		MinecraftClient.getInstance().profiler.popPush("updatechunks");
	}

	public int renderBlockEntities(PoseStack modelView, double cameraX, double cameraY, double cameraZ, float tickDelta, boolean hasEntityFrustum, boolean lightsOnly) {
		getLevel().getProfiler().push("build blockentities");

		int shadowBlockEntities = 0;
		BoxCuller culler = null;
		if (hasEntityFrustum) {
			culler = new BoxCuller(halfPlaneLength * (renderDistanceMultiplier * entityShadowDistanceMultiplier));
			culler.setPosition(cameraX, cameraY, cameraZ);
		}

		for (BlockEntity entity : visibleBlockEntities) {
			if (lightsOnly && entity.getBlockState().getLightEmission() == 0) {
				continue;
			}

			BlockPos pos = entity.getBlockPos();
			if (hasEntityFrustum) {
				if (culler.isCulled(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)) {
					continue;
				}
			}
			modelView.pushPose();
			modelView.translate(pos.getX() - cameraX, pos.getY() - cameraY, pos.getZ() - cameraZ);
			MinecraftClient.getInstance().getBlockEntityRenderDispatcher().render(entity, tickDelta, modelView, buffers.bufferSource());
			modelView.popPose();

			shadowBlockEntities++;
		}

		getLevel().getProfiler().pop();

		return shadowBlockEntities;
	}

	private int renderEntities(LevelRendererAccessor levelRenderer, EntityRenderDispatcher dispatcher, MultiBufferSource.BufferSource bufferSource, PoseStack modelView, float tickDelta, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
		MinecraftClient.getInstance().profiler.push("cull");

		List<Entity> renderedEntities = new ArrayList<>(32);

		// TODO: I'm sure that this can be improved / optimized.
		for (Entity entity : LevelRenderer) {
			if (!dispatcher.shouldRender(entity, frustum, cameraX, cameraY, cameraZ) || entity.isSpectator()) {
				continue;
			}

			renderedEntities.add(entity);
		}

		MinecraftClient.getInstance().profiler.popPush("sort");

		// Sort the entities by type first in order to allow vanilla's entity batching system to work better.
		renderedEntities.sort(Comparator.comparingInt(entity -> entity.getType().hashCode()));

		MinecraftClient.getInstance().profiler.popPush("build entity geometry");

		for (Entity entity : renderedEntities) {
			float realTickDelta = MinecraftClient.getInstance().level.tickRateManager().isEntityFrozen(entity) ? tickDelta : CapturedRenderingState.INSTANCE.getRealTickDelta();
			levelRenderer.invokeRenderEntity(entity, cameraX, cameraY, cameraZ, realTickDelta, modelView, bufferSource);
		}

		MinecraftClient.getInstance().profiler.pop();

		return renderedEntities.size();
	}

	private int renderPlayerEntity(LevelRendererAccessor levelRenderer, EntityRenderDispatcher dispatcher, MultiBufferSource.BufferSource bufferSource, PoseStack modelView, float tickDelta, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
		MinecraftClient.getInstance().profiler.push("cull");

		Entity player = MinecraftClient.getInstance().player;

		int shadowEntities = 0;

		if (!dispatcher.shouldRender(player, frustum, cameraX, cameraY, cameraZ) || player.isSpectator()) {
			MinecraftClient.getInstance().profiler.pop();
			return 0;
		}

		MinecraftClient.getInstance().profiler.popPush("build geometry");

		if (!player.getPassengers().isEmpty()) {
			for (int i = 0; i < player.getPassengers().size(); i++) {
				float realTickDelta = MinecraftClient.getInstance().level.tickRateManager().isEntityFrozen(player.getPassengers().get(i)) ? tickDelta : CapturedRenderingState.INSTANCE.getRealTickDelta();
				levelRenderer.invokeRenderEntity(player.getPassengers().get(i), cameraX, cameraY, cameraZ, realTickDelta, modelView, bufferSource);
				shadowEntities++;
			}
		}

		if (player.getVehicle() != null) {
			float realTickDelta = MinecraftClient.getInstance().level.tickRateManager().isEntityFrozen(player.getVehicle()) ? tickDelta : CapturedRenderingState.INSTANCE.getRealTickDelta();
			levelRenderer.invokeRenderEntity(player.getVehicle(), cameraX, cameraY, cameraZ, realTickDelta, modelView, bufferSource);
			shadowEntities++;
		}

		float realTickDelta = MinecraftClient.getInstance().level.tickRateManager().isEntityFrozen(player) ? tickDelta : CapturedRenderingState.INSTANCE.getRealTickDelta();
		levelRenderer.invokeRenderEntity(player, cameraX, cameraY, cameraZ, realTickDelta, modelView, bufferSource);

		shadowEntities++;

		MinecraftClient.getInstance().profiler.pop();

		return shadowEntities;
	}

	private void copyPreTranslucentDepth(LevelRendererAccessor levelRenderer) {
		MinecraftClient.getInstance().profiler.popPush("translucent depth copy");

		targets.copyPreTranslucentDepth();
	}

	public void addDebugText(List<String> messages) {
		if (IrisVideoSettings.getOverriddenShadowDistance(IrisVideoSettings.shadowDistance) == 0) {
			messages.add("[" + Iris.MODNAME + "] Shadow Maps: off, shadow distance 0");
			return;
		}

		if (Iris.getIrisConfig().areDebugOptionsEnabled()) {
			messages.add("[" + Iris.MODNAME + "] Shadow Maps: " + debugStringOverall);
			messages.add("[" + Iris.MODNAME + "] Shadow Distance Terrain: " + terrainFrustumHolder.getDistanceInfo() + " Entity: " + entityFrustumHolder.getDistanceInfo());
			messages.add("[" + Iris.MODNAME + "] Shadow Culling Terrain: " + terrainFrustumHolder.getCullingInfo() + " Entity: " + entityFrustumHolder.getCullingInfo());
			messages.add("[" + Iris.MODNAME + "] Shadow Projection: " + getProjectionInfo());
			messages.add("[" + Iris.MODNAME + "] Shadow Terrain: " + debugStringTerrain
				+ (shouldRenderTerrain ? "" : " (no terrain) ") + (shouldRenderTranslucent ? "" : "(no translucent)"));
			messages.add("[" + Iris.MODNAME + "] Shadow Entities: " + getEntitiesDebugString());
			messages.add("[" + Iris.MODNAME + "] Shadow Block Entities: " + getBlockEntitiesDebugString());

			if (buffers instanceof DrawCallTrackingRenderBuffers drawCallTracker && (shouldRenderEntities || shouldRenderPlayer)) {
				messages.add("[" + Iris.MODNAME + "] Shadow Entity Batching: " + BatchingDebugMessageHelper.getDebugMessage(drawCallTracker));
			}
		} else {
			messages.add("[" + Iris.MODNAME + "] Shadow info: " + debugStringTerrain);
			messages.add("[" + Iris.MODNAME + "] E: " + renderedShadowEntities);
			messages.add("[" + Iris.MODNAME + "] BE: " + renderedShadowBlockEntities);
		}
	}

	private String getProjectionInfo() {
		return "Near: " + nearPlane + " Far: " + farPlane + " distance " + halfPlaneLength;
	}

	private String getEntitiesDebugString() {
		return (shouldRenderEntities || shouldRenderPlayer) ? (renderedShadowEntities + "/" + MinecraftClient.getInstance().level.getEntityCount()) : "disabled by pack";
	}

	private String getBlockEntitiesDebugString() {
		return (shouldRenderBlockEntities || shouldRenderLightBlockEntities) ? renderedShadowBlockEntities + "" : "disabled by pack"; // TODO: + "/" + MinecraftClient.getInstance().world.blockEntities.size();
	}

	public void destroy() {
		((MemoryTrackingRenderBuffers) buffers).freeAndDeleteBuffers();
	}

	private record MipmapPass(int texture, int targetFilteringMode) {


	}
}
