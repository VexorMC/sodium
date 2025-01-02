package net.irisshaders.iris.uniforms;

import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.gui.option.IrisVideoSettings;
import net.irisshaders.iris.helpers.JomlConversions;
import net.irisshaders.iris.mixin.GameRendererAccessor;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LightningBoltEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Objects;
import java.util.stream.StreamSupport;

import static net.irisshaders.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

public class IrisExclusiveUniforms {
	private static final Vector3d ZERO = new Vector3d(0);

	public static void addIrisExclusiveUniforms(UniformHolder uniforms) {
		WorldInfoUniforms.addWorldInfoUniforms(uniforms);

		uniforms.uniform1i(UniformUpdateFrequency.PER_TICK, "currentColorSpace", () -> IrisVideoSettings.colorSpace.ordinal());

		//All Iris-exclusive uniforms (uniforms which do not exist in either OptiFine or ShadersMod) should be registered here.
		uniforms.uniform1f(UniformUpdateFrequency.PER_FRAME, "thunderStrength", IrisExclusiveUniforms::getThunderStrength);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "currentPlayerHealth", IrisExclusiveUniforms::getCurrentHealth);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "maxPlayerHealth", IrisExclusiveUniforms::getMaxHealth);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "currentPlayerHunger", IrisExclusiveUniforms::getCurrentHunger);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "maxPlayerHunger", () -> 20);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "currentPlayerArmor", IrisExclusiveUniforms::getCurrentArmor);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "maxPlayerArmor", () -> 50);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "currentPlayerAir", IrisExclusiveUniforms::getCurrentAir);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "maxPlayerAir", IrisExclusiveUniforms::getMaxAir);
		uniforms.uniform1b(UniformUpdateFrequency.PER_FRAME, "firstPersonCamera", IrisExclusiveUniforms::isFirstPersonCamera);
		uniforms.uniform1b(UniformUpdateFrequency.PER_TICK, "isSpectator", IrisExclusiveUniforms::isSpectator);
		uniforms.uniform1i(PER_FRAME, "currentSelectedBlockId", IrisExclusiveUniforms::getCurrentSelectedBlockId);
		uniforms.uniform3f(PER_FRAME, "currentSelectedBlockPos", IrisExclusiveUniforms::getCurrentSelectedBlockPos);
		uniforms.uniform3d(UniformUpdateFrequency.PER_FRAME, "eyePosition", IrisExclusiveUniforms::getEyePosition);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "cloudTime", CapturedRenderingState.INSTANCE::getCloudTime);
		uniforms.uniform3d(UniformUpdateFrequency.PER_FRAME, "relativeEyePosition", () -> CameraUniforms.getUnshiftedCameraPosition().sub(getEyePosition()));
		uniforms.uniform3d(UniformUpdateFrequency.PER_FRAME, "playerLookVector", () -> {
			if (MinecraftClient.getInstance().getCameraEntity() instanceof LivingEntity livingEntity) {
				return JomlConversions.fromVec3(livingEntity.getCameraPosVec(CapturedRenderingState.INSTANCE.getTickDelta()));
			} else {
				return ZERO;
			}
		});
		uniforms.uniform3d(UniformUpdateFrequency.PER_FRAME, "playerBodyVector", () -> JomlConversions.fromVec3(MinecraftClient.getInstance().getCameraEntity().getPos()));
		Vector4f zero = new Vector4f(0, 0, 0, 0);
		uniforms.uniform4f(UniformUpdateFrequency.PER_TICK, "lightningBoltPosition", () -> {
			if (MinecraftClient.getInstance().world != null) {
				return StreamSupport.stream(MinecraftClient.getInstance().world.entities.spliterator(), false).filter(bolt -> bolt instanceof LightningBoltEntity).findAny().map(bolt -> {
					Vector3d unshiftedCameraPosition = CameraUniforms.getUnshiftedCameraPosition();
					Vec3d vec3 = bolt.getPos();
					return new Vector4f((float) (vec3.x - unshiftedCameraPosition.x), (float) (vec3.y - unshiftedCameraPosition.y), (float) (vec3.z - unshiftedCameraPosition.z), 1);
				}).orElse(zero);
			} else {
				return zero;
			}
		});
	}

	private static int getCurrentSelectedBlockId() {
		BlockHitResult hitResult = MinecraftClient.getInstance().result;
		if (MinecraftClient.getInstance().world != null && ((GameRendererAccessor) MinecraftClient.getInstance().gameRenderer).shouldRenderBlockOutlineA() && hitResult != null && hitResult.type == BlockHitResult.Type.BLOCK) {
			BlockPos blockPos4 = ((BlockHitResult) hitResult).getBlockPos();
			BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockPos4);
			if (blockState.getBlock().getMaterial() != Material.AIR && MinecraftClient.getInstance().world.getWorldBorder().contains(blockPos4)) {
				return WorldRenderingSettings.INSTANCE.getBlockStateIds().getInt(blockState);
			}
		}

		return 0;
	}

	private static Vector3f getCurrentSelectedBlockPos() {
        BlockHitResult hitResult = MinecraftClient.getInstance().result;
		if (MinecraftClient.getInstance().world != null && ((GameRendererAccessor) MinecraftClient.getInstance().gameRenderer).shouldRenderBlockOutlineA() && hitResult != null && hitResult.type == BlockHitResult.Type.BLOCK) {
			BlockPos blockPos4 = ((BlockHitResult) hitResult).getBlockPos();
			BlockPos finalBpos = blockPos4.subtract(MinecraftClient.getInstance().getCameraEntity().getBlockPos());
            return new Vector3f(finalBpos.getX(), finalBpos.getY(), finalBpos.getZ());
		}

		return new Vector3f(-256.0f);
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

        int v = MinecraftClient.getInstance().player.getAir();
        int w = MathHelper.ceil((double)(v - 2) * (double)10.0F / (double)300.0F);
        int x = MathHelper.ceil((double)v * (double)10.0F / (double)300.0F) - w;

        return (float) MinecraftClient.getInstance().player.getAir() / (float) w + x;
	}

	private static float getCurrentArmor() {
		if (MinecraftClient.getInstance().player == null ) {
			return -1;
		}

		return MinecraftClient.getInstance().player.getArmorProtectionValue() / 50.0f;
	}

	private static float getMaxAir() {
		if (MinecraftClient.getInstance().player == null ) {
			return -1;
		}
        int v = MinecraftClient.getInstance().player.getAir();
        int w = MathHelper.ceil((double)(v - 2) * (double)10.0F / (double)300.0F);
        int x = MathHelper.ceil((double)v * (double)10.0F / (double)300.0F) - w;

		return w + x;
	}

	private static float getMaxHealth() {
		if (MinecraftClient.getInstance().player == null ) {
			return -1;
		}

		return MinecraftClient.getInstance().player.getMaxHealth();
	}


	private static boolean isFirstPersonCamera() {
		// If camera type is not explicitly third-person, assume it's first-person.
		return switch (MinecraftClient.getInstance().options.perspective) {
			case 1, 2 -> false;
			default -> true;
		};
	}

	private static boolean isSpectator() {
		return MinecraftClient.getInstance().player.isSpectator();
	}

	private static Vector3d getEyePosition() {
		Objects.requireNonNull(MinecraftClient.getInstance().getCameraEntity());
		Vec3d pos = MinecraftClient.getInstance().getCameraEntity().getCameraPosVec(CapturedRenderingState.INSTANCE.getTickDelta());
		return new Vector3d(pos.x, pos.y, pos.z);
	}

	public static class WorldInfoUniforms {
		public static void addWorldInfoUniforms(UniformHolder uniforms) {
			ClientWorld level = MinecraftClient.getInstance().world;
			// TODO: Use level.dimensionType() coordinates for 1.18!
			uniforms.uniform1i(UniformUpdateFrequency.PER_FRAME, "bedrockLevel", () -> 0);
			uniforms.uniform1f(UniformUpdateFrequency.PER_FRAME, "cloudHeight", () -> SodiumClientMod.options().quality.cloudHeight);
			uniforms.uniform1i(UniformUpdateFrequency.PER_FRAME, "heightLimit", () -> 256);
			uniforms.uniform1i(UniformUpdateFrequency.PER_FRAME, "logicalHeightLimit", () -> 256);
			uniforms.uniform1b(UniformUpdateFrequency.PER_FRAME, "hasCeiling", () -> false);
			uniforms.uniform1b(UniformUpdateFrequency.PER_FRAME, "hasSkylight", () -> {
				if (level != null) {
					return level.dimension.hasNoSkylight();
				} else {
					return true;
				}
			});
			uniforms.uniform1f(UniformUpdateFrequency.PER_FRAME, "ambientLight", () -> {
				if (level != null) {
					return level.getAmbientDarkness();
				} else {
					return 0f;
				}
			});

		}
	}
}
