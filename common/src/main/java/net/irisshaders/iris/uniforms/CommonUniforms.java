package net.irisshaders.iris.uniforms;

import com.mojang.blaze3d.platform.GlStateManager;
import net.irisshaders.iris.compat.dh.DHCompat;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.gl.state.StateUpdateNotifiers;
import net.irisshaders.iris.gl.uniform.DynamicUniformHolder;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.helpers.JomlConversions;
import net.irisshaders.iris.layer.GbufferPrograms;
import net.irisshaders.iris.mixin.GlStateManagerAccessor;
import net.irisshaders.iris.mixin.statelisteners.BooleanStateAccessor;
import net.irisshaders.iris.mixin.texture.TextureAtlasAccessor;
import net.irisshaders.iris.mixinterface.LocalPlayerInterface;
import net.irisshaders.iris.pbr.TextureInfoCache;
import net.irisshaders.iris.pbr.TextureInfoCache.TextureInfo;
import net.irisshaders.iris.pbr.TextureTracker;
import net.irisshaders.iris.shaderpack.IdMap;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.uniforms.transforms.SmoothedFloat;
import net.irisshaders.iris.uniforms.transforms.SmoothedVec2f;
import net.minecraft.block.material.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector4f;
import org.joml.Vector4i;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import static net.irisshaders.iris.gl.uniform.UniformUpdateFrequency.ONCE;
import static net.irisshaders.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;
import static net.irisshaders.iris.gl.uniform.UniformUpdateFrequency.PER_TICK;

public final class CommonUniforms {
	private static final MinecraftClient client = MinecraftClient.getInstance();
	private static final Vector2i ZERO_VECTOR_2i = new Vector2i();
	private static final Vector4i ZERO_VECTOR_4i = new Vector4i(0, 0, 0, 0);
	private static final Vector3d ZERO_VECTOR_3d = new Vector3d();

	static {
		GbufferPrograms.init();
	}

	private CommonUniforms() {
		// no construction allowed
	}

	// Needs to use a LocationalUniformHolder as we need it for the common uniforms
	public static void addDynamicUniforms(DynamicUniformHolder uniforms, FogMode fogMode) {
		ExternallyManagedUniforms.addExternallyManagedUniforms117(uniforms);
		FogUniforms.addFogUniforms(uniforms, fogMode);
		IrisInternalUniforms.addFogUniforms(uniforms, fogMode);

		// This is a fallback for when entityId via attributes cannot be used. (lightning)
		uniforms.uniform1i("entityId", CapturedRenderingState.INSTANCE::getCurrentRenderedEntity, StateUpdateNotifiers.fallbackEntityNotifier);

		// TODO: OptiFine doesn't think that atlasSize is a "dynamic" uniform,
		//       but we do. How will custom uniforms depending on atlasSize work?
		//
		// Note: on 1.17+ we don't need to reset this when textures are bound, since
		// the shader will always be setup (and therefore uniforms will be re-uploaded)
		// after the texture is changed and before rendering starts.
		uniforms.uniform2i("atlasSize", () -> {
			int glId = GlStateManager.activeTexture();

			AbstractTexture texture = TextureTracker.INSTANCE.getTexture(glId);
			if (texture instanceof SpriteAtlasTexture atlas) {
				return new Vector2i(MinecraftClient.getMaxTextureSize(), MinecraftClient.getMaxTextureSize());
			}

			return ZERO_VECTOR_2i;
		}, listener -> {
		});

		uniforms.uniform2i("gtextureSize", () -> {
			int glId = GlStateManagerAccessor.getTEXTURES()[0].binding;

			TextureInfo info = TextureInfoCache.INSTANCE.getInfo(glId);
			return new Vector2i(info.getWidth(), info.getHeight());

		}, StateUpdateNotifiers.bindTextureNotifier);

		uniforms.uniform4i("blendFunc", () -> {
			GlStateManager.BlendFuncState blend = GlStateManagerAccessor.getBLEND();

			if (((BooleanStateAccessor) blend.capState).isEnabled()) {
				return new Vector4i(blend.srcFactorRgb, blend.dstFactorRgb, blend.srcFactorAlpha, blend.dstFactorAlpha);
			} else {
				return ZERO_VECTOR_4i;
			}
		}, StateUpdateNotifiers.blendFuncNotifier);

		uniforms.uniform1i("renderStage", () -> GbufferPrograms.getCurrentPhase().ordinal(), StateUpdateNotifiers.phaseChangeNotifier);
	}

	public static void addCommonUniforms(DynamicUniformHolder uniforms, IdMap idMap, PackDirectives directives, FrameUpdateNotifier updateNotifier, FogMode fogMode) {
		CommonUniforms.addNonDynamicUniforms(uniforms, idMap, directives, updateNotifier);
		CommonUniforms.addDynamicUniforms(uniforms, fogMode);
	}

	public static void addNonDynamicUniforms(UniformHolder uniforms, IdMap idMap, PackDirectives directives, FrameUpdateNotifier updateNotifier) {
		CameraUniforms.addCameraUniforms(uniforms, updateNotifier);
		ViewportUniforms.addViewportUniforms(uniforms);
		WorldTimeUniforms.addWorldTimeUniforms(uniforms);
		SystemTimeUniforms.addSystemTimeUniforms(uniforms);
		BiomeUniforms.addBiomeUniforms(uniforms);
		new CelestialUniforms(directives.getSunPathRotation()).addCelestialUniforms(uniforms);
		IrisExclusiveUniforms.addIrisExclusiveUniforms(uniforms);
		IrisTimeUniforms.addTimeUniforms(uniforms);
		MatrixUniforms.addMatrixUniforms(uniforms, directives);
		IdMapUniforms.addIdMapUniforms(updateNotifier, uniforms, idMap, directives.isOldHandLight());
		CommonUniforms.generalCommonUniforms(uniforms, updateNotifier, directives);
	}

	public static void generalCommonUniforms(UniformHolder uniforms, FrameUpdateNotifier updateNotifier, PackDirectives directives) {
		ExternallyManagedUniforms.addExternallyManagedUniforms117(uniforms);

		SmoothedVec2f eyeBrightnessSmooth = new SmoothedVec2f(directives.getEyeBrightnessHalfLife(), directives.getEyeBrightnessHalfLife(), CommonUniforms::getEyeBrightness, updateNotifier);

		uniforms
			.uniform1b(PER_FRAME, "hideGUI", () -> client.options.hudHidden)
			.uniform1b(PER_FRAME, "isRightHanded", () -> true)
			.uniform1i(PER_FRAME, "isEyeInWater", CommonUniforms::isEyeInWater)
			.uniform1f(PER_FRAME, "blindness", CommonUniforms::getBlindness)
			.uniform1f(PER_FRAME, "darknessFactor", CommonUniforms::getDarknessFactor)
			.uniform1f(PER_FRAME, "darknessLightFactor", CapturedRenderingState.INSTANCE::getDarknessLightFactor)
			.uniform1f(PER_FRAME, "nightVision", CommonUniforms::getNightVision)
			.uniform1b(PER_FRAME, "is_sneaking", CommonUniforms::isSneaking)
			.uniform1b(PER_FRAME, "is_sprinting", CommonUniforms::isSprinting)
			.uniform1b(PER_FRAME, "is_hurt", CommonUniforms::isHurt)
			.uniform1b(PER_FRAME, "is_invisible", CommonUniforms::isInvisible)
			.uniform1b(PER_FRAME, "is_burning", CommonUniforms::isBurning)
			.uniform1b(PER_FRAME, "is_on_ground", CommonUniforms::isOnGround)
			// TODO: Do we need to clamp this to avoid fullbright breaking shaders? Or should shaders be able to detect
			//       that the player is trying to turn on fullbright?
			.uniform1f(PER_FRAME, "screenBrightness", () -> client.options.gamma)
			// just a dummy value for shaders where entityColor isn't supplied through a vertex attribute (and thus is
			// not available) - suppresses warnings. See AttributeShaderTransformer for the actual entityColor code.
			.uniform4f(ONCE, "entityColor", () -> new Vector4f(0, 0, 0, 0))
			.uniform1i(ONCE, "blockEntityId", () -> -1)
			.uniform1i(ONCE, "currentRenderedItemId", () -> -1)
			.uniform1f(ONCE, "pi", () -> Math.PI)
			.uniform1f(PER_TICK, "playerMood", CommonUniforms::getPlayerMood)
			.uniform1f(PER_TICK, "constantMood", CommonUniforms::getConstantMood)
			.uniform2i(PER_FRAME, "eyeBrightness", CommonUniforms::getEyeBrightness)
			.uniform2i(PER_FRAME, "eyeBrightnessSmooth", () -> {
				Vector2f smoothed = eyeBrightnessSmooth.get();
				return new Vector2i((int) smoothed.x(), (int) smoothed.y());
			})
			.uniform1f(PER_TICK, "rainStrength", CommonUniforms::getRainStrength)
			.uniform1f(PER_TICK, "wetness", new SmoothedFloat(directives.getWetnessHalfLife(), directives.getDrynessHalfLife(), CommonUniforms::getRainStrength, updateNotifier))
			.uniform3d(PER_FRAME, "skyColor", CommonUniforms::getSkyColor)
			.uniform1f(PER_FRAME, "dhFarPlane", DHCompat::getFarPlane)
			.uniform1f(PER_FRAME, "dhNearPlane", DHCompat::getNearPlane)
			.uniform1i(PER_FRAME, "dhRenderDistance", DHCompat::getRenderDistance);
	}

	private static boolean isOnGround() {
		return client.player != null && client.player.onGround;
	}

	private static boolean isHurt() {
		if (client.player != null) {
			return client.player.hurtTime > 0; // Do not use isHurt, that's not what we want!
		} else {
			return false;
		}
	}

	private static boolean isInvisible() {
		if (client.player != null) {
			return client.player.isInvisible();
		} else {
			return false;
		}
	}

	private static boolean isBurning() {
		if (client.player != null) {
			return client.player.isOnFire();
		} else {
			return false;
		}
	}

	private static boolean isSneaking() {
		if (client.player != null) {
			return client.player.isSneaking();
		} else {
			return false;
		}
	}

	private static boolean isSprinting() {
		if (client.player != null) {
			return client.player.isSprinting();
		} else {
			return false;
		}
	}

	private static Vector3d getSkyColor() {
		if (client.world == null || client.getCameraEntity() == null) {
			return ZERO_VECTOR_3d;
		}

		return JomlConversions.fromVec3(client.world.getCloudColor(CapturedRenderingState.INSTANCE.getTickDelta()));
	}

	static float getBlindness() {
		Entity cameraEntity = client.getCameraEntity();

		if (cameraEntity instanceof LivingEntity) {
			StatusEffectInstance blindness = ((LivingEntity) cameraEntity).getEffectInstance(StatusEffect.BLINDNESS);

			if (blindness != null) {
				// Guessing that this is what OF uses, based on how vanilla calculates the fog value in FogRenderer
				// TODO: Add this to ShaderDoc
				if (blindness.isPermanent()) {
					return 1.0f;
				} else {
					return Math.clamp(0.0F, 1.0F, blindness.getDuration() / 20.0F);
				}
			}
		}

		return 0.0F;
	}

	static float getDarknessFactor() {
		Entity cameraEntity = client.getCameraEntity();

		if (cameraEntity instanceof LivingEntity) {
            StatusEffectInstance darkness = ((LivingEntity) cameraEntity).getEffectInstance(StatusEffect.BLINDNESS);

			if (darkness != null) {
				return darkness.getAmplifier();
			}
		}

		return 0.0F;
	}

	private static float getPlayerMood() {
		if (!(client.getCameraEntity() instanceof ClientPlayerEntity)) {
			return 0.0F;
		}

		// This should always be 0 to 1 anyways but just making sure
        return 0f;
	}

	private static float getConstantMood() {
		if (!(client.getCameraEntity() instanceof ClientPlayerEntity)) {
			return 0.0F;
		}

		// This should always be 0 to 1 anyways but just making sure
        return 0f;
	}

	static float getRainStrength() {
		if (client.world == null) {
			return 0f;
		}

		// Note: Ensure this is in the range of 0 to 1 - some custom servers send out of range values.
		return Math.clamp(0.0F, 1.0F,
			client.world.getRainGradient(CapturedRenderingState.INSTANCE.getTickDelta()));
	}

	private static Vector2i getEyeBrightness() {
		if (client.getCameraEntity() == null || client.world == null) {
			return ZERO_VECTOR_2i;
		}

		Vec3d feet = client.getCameraEntity().getPos();
        Vec3d eyes = new Vec3d(feet.x, client.getCameraEntity().getEyeHeight(), feet.z);
		BlockPos eyeBlockPos = new BlockPos(eyes);

		int blockLight = client.world.getLightAtPos(LightType.BLOCK, eyeBlockPos);
		int skyLight = client.world.getLightAtPos(LightType.SKY, eyeBlockPos);

		return new Vector2i(blockLight * 16, skyLight * 16);
	}

	private static float getNightVision() {
		Entity cameraEntity = client.getCameraEntity();

		if (cameraEntity instanceof LivingEntity livingEntity) {

			try {
				// See MixinGameRenderer#iris$safecheckNightvisionStrength.
				//
				// We modify the behavior of getNightVisionScale so that it's safe for us to call it even on entities
				// that don't have the effect, allowing us to pick up modified night vision strength values from mods
				// like Origins.
				//
				// See: https://github.com/apace100/apoli/blob/320b0ef547fbbf703de7154f60909d30366f6500/src/main/java/io/github/apace100/apoli/mixin/GameRendererMixin.java#L153
				float nightVisionStrength =
                        getNightVisionStrength(livingEntity, CapturedRenderingState.INSTANCE.getTickDelta());

				if (nightVisionStrength > 0) {
					// Just protecting against potential weird mod behavior
					return Math.clamp(0.0F, 1.0F, nightVisionStrength);
				}
			} catch (NullPointerException e) {
				// If our injection didn't get applied, a NullPointerException will occur from calling that method if
				// the entity doesn't currently have night vision. This isn't pretty but it's functional.
				return 0.0F;
			}
		}


		return 0.0F;
	}


    private static float getNightVisionStrength(LivingEntity entity, float tickDelta) {
        int i = entity.getEffectInstance(StatusEffect.NIGHTVISION).getDuration();
        return i > 200 ? 1.0F : 0.7F + MathHelper.sin(((float)i - tickDelta) * (float) java.lang.Math.PI * 0.2F) * 0.3F;
    }

	static int isEyeInWater() {
		// Note: With certain utility / cheat mods, this method will return air even when the player is submerged when
		// the "No Overlay" feature is enabled.
		//
		// I'm not sure what the best way to deal with this is, but the current approach seems to be an acceptable one -
		// after all, disabling the overlay results in the intended effect of it not really looking like you're
		// underwater on most shaderpacks. For now, I will leave this as-is, but it is something to keep in mind.
		Entity submersionType = client.getCameraEntity();

		if (submersionType.isSubmergedIn(Material.WATER)) {
			return 1;
		} else if (submersionType.isSubmergedIn(Material.LAVA)) {
			return 2;
		} else {
			return 0;
		}
	}
}
