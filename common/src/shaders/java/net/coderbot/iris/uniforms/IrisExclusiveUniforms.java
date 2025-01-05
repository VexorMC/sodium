package net.coderbot.iris.uniforms;

import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.mixin.DimensionTypeAccessor;
import net.coderbot.iris.vendored.joml.Math;
import net.coderbot.iris.vendored.joml.Vector3d;
import net.coderbot.iris.vendored.joml.Vector4f;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LightningBoltEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;
import java.util.stream.StreamSupport;

public class IrisExclusiveUniforms {
	public static void addIrisExclusiveUniforms(UniformHolder uniforms) {
		WorldInfoUniforms.addWorldInfoUniforms(uniforms);

		//All Iris-exclusive uniforms (uniforms which do not exist in either OptiFine or ShadersMod) should be registered here.
		uniforms.uniform1f(UniformUpdateFrequency.PER_FRAME, "thunderStrength", IrisExclusiveUniforms::getThunderStrength);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "currentPlayerHealth", IrisExclusiveUniforms::getCurrentHealth);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "maxPlayerHealth", IrisExclusiveUniforms::getMaxHealth);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "currentPlayerHunger", IrisExclusiveUniforms::getCurrentHunger);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "maxPlayerHunger", () -> 20);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "currentPlayerAir", IrisExclusiveUniforms::getCurrentAir);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "maxPlayerAir", IrisExclusiveUniforms::getMaxAir);
		uniforms.uniform1b(UniformUpdateFrequency.PER_FRAME, "firstPersonCamera", IrisExclusiveUniforms::isFirstPersonCamera);
		uniforms.uniform1b(UniformUpdateFrequency.PER_TICK, "isSpectator", IrisExclusiveUniforms::isSpectator);
		uniforms.uniform3d(UniformUpdateFrequency.PER_FRAME, "eyePosition", IrisExclusiveUniforms::getEyePosition);
		Vector4f zero = new Vector4f(0, 0, 0, 0);
		uniforms.uniform4f(UniformUpdateFrequency.PER_TICK, "lightningBoltPosition", () -> {
			if (MinecraftClient.getInstance().world != null) {
				return StreamSupport.stream(MinecraftClient.getInstance().world.getLoadedEntities().spliterator(), false).filter(bolt -> bolt instanceof LightningBoltEntity).findAny().map(bolt -> {
					Vector3d unshiftedCameraPosition = CameraUniforms.getUnshiftedCameraPosition();
					Vec3d vec3 = bolt.getPos();
                    return new Vector4f((float) (vec3.x - unshiftedCameraPosition.x), (float) (vec3.y - unshiftedCameraPosition.y), (float) (vec3.z - unshiftedCameraPosition.z), 1);
				}).orElse(zero);
			} else {
				return zero;
			}
		});
	}

	private static float getThunderStrength() {
		// Note: Ensure this is in the range of 0 to 1 - some custom servers send out of range values.
		return Math.clamp(0.0F, 1.0F,
			MinecraftClient.getInstance().world.getThunderGradient(CapturedRenderingState.INSTANCE.getTickDelta()));
	}

	private static float getCurrentHealth() {
		if (MinecraftClient.getInstance().player == null) {
			return -1;
		}

		return MinecraftClient.getInstance().player.getHealth() / MinecraftClient.getInstance().player.getMaxHealth();
	}

	private static float getCurrentHunger() {
		if (MinecraftClient.getInstance().player == null) {
			return -1;
		}

		return MinecraftClient.getInstance().player.getAbsorption() / 20f;
	}

	private static float getCurrentAir() {
		if (MinecraftClient.getInstance().player == null) {
			return -1;
		}

		return (float) MinecraftClient.getInstance().player.getAir();
	}

	private static float getMaxAir() {
		if (MinecraftClient.getInstance().player == null) {
			return -1;
		}

		return (float) MinecraftClient.getInstance().player.getAir();
	}

	private static float getMaxHealth() {
		if (MinecraftClient.getInstance().player == null) {
			return -1;
		}

		return MinecraftClient.getInstance().player.getMaxHealth();
	}

	private static boolean isFirstPersonCamera() {
		// If camera type is not explicitly third-person, assume it's first-person.
		switch (MinecraftClient.getInstance().options.perspective) {
			case 2:
			case 3:
				return false;
			default: return true;
		}
	}

	private static boolean isSpectator() {
		return MinecraftClient.getInstance().player.isSpectator();
	}

	private static Vector3d getEyePosition() {
		Objects.requireNonNull(MinecraftClient.getInstance().getCameraEntity());
		return new Vector3d(MinecraftClient.getInstance().getCameraEntity().getPos().x, MinecraftClient.getInstance().getCameraEntity().getEyeHeight(), MinecraftClient.getInstance().getCameraEntity().getPos().z);
	}

	public static class WorldInfoUniforms {
		public static void addWorldInfoUniforms(UniformHolder uniforms) {
			ClientWorld level = MinecraftClient.getInstance().world;
			// TODO: Use level.dimensionType() coordinates for 1.18!
			uniforms.uniform1i(UniformUpdateFrequency.PER_FRAME, "bedrockLevel", () -> 0);
			uniforms.uniform1i(UniformUpdateFrequency.PER_FRAME, "heightLimit", () -> {
				if (level != null) {
					return level.getMaxBuildHeight();
				} else {
					return 256;
				}
			});
			uniforms.uniform1b(UniformUpdateFrequency.PER_FRAME, "hasCeiling", () -> {
				if (level != null) {
					return level.dimension.hasNoSkylight();
				} else {
					return false;
				}
			});
			uniforms.uniform1b(UniformUpdateFrequency.PER_FRAME, "hasSkylight", () -> {
				if (level != null) {
                    return !level.dimension.hasNoSkylight();
				} else {
					return true;
				}
			});
			uniforms.uniform1f(UniformUpdateFrequency.PER_FRAME, "ambientLight", () -> {
				if (level != null) {
                    return level.dimension.getLightLevelToBrightness()[0];
                } else {
					return 0f;
				}
			});

		}
	}
}
