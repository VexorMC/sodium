package net.coderbot.iris.uniforms;

import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import net.coderbot.iris.gl.uniform.DynamicUniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.shaderpack.IdMap;
import net.coderbot.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.api.v0.item.IrisItemLightProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.vector.Vector3f;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

public final class IdMapUniforms {

	private IdMapUniforms() {
	}

	public static void addIdMapUniforms(FrameUpdateNotifier notifier, DynamicUniformHolder uniforms, IdMap idMap, boolean isOldHandLight) {
		HeldItemSupplier mainHandSupplier = new HeldItemSupplier(idMap.getItemIdMap(), isOldHandLight);
		notifier.addListener(mainHandSupplier::update);

		uniforms
			.uniform1i(UniformUpdateFrequency.PER_FRAME, "heldItemId", mainHandSupplier::getIntID)
			.uniform1i(PER_FRAME, "heldBlockLightValue", mainHandSupplier::getLightValue);
		// TODO: Figure out API.
			//.uniformVanilla3f(PER_FRAME, "heldBlockLightColor", mainHandSupplier::getLightColor)
			//.uniformVanilla3f(PER_FRAME, "heldBlockLightColor2", offHandSupplier::getLightColor);

		uniforms.uniform1i("entityId", CapturedRenderingState.INSTANCE::getCurrentRenderedEntity,
				CapturedRenderingState.INSTANCE.getEntityIdNotifier());

		uniforms.uniform1i("blockEntityId", CapturedRenderingState.INSTANCE::getCurrentRenderedBlockEntity,
				CapturedRenderingState.INSTANCE.getBlockEntityIdNotifier());
	}

	/**
	 * Provides the currently held item, and it's light value, in the given hand as a uniform. Uses the item.properties ID map to map the item
	 * to an integer, and the old hand light value to map offhand to main hand.
	 */
	private static class HeldItemSupplier {
		private final Object2IntFunction<NamespacedId> itemIdMap;
		private final boolean applyOldHandLight;
		private int intID;
		private int lightValue;
		private Vector3f lightColor;

		HeldItemSupplier(Object2IntFunction<NamespacedId> itemIdMap, boolean shouldApplyOldHandLight) {
			this.itemIdMap = itemIdMap;
			this.applyOldHandLight = shouldApplyOldHandLight;
		}

		private void invalidate() {
			intID = -1;
			lightValue = 0;
			lightColor = IrisItemLightProvider.DEFAULT_LIGHT_COLOR;
		}

		public void update() {
			ClientPlayerEntity player = MinecraftClient.getInstance().player;

			if (player == null) {
				// Not valid when the player doesn't exist
				invalidate();
				return;
			}

			ItemStack heldStack = player.getMainHandStack();

			if (heldStack == null) {
				invalidate();
				return;
			}

			Item heldItem = heldStack.getItem();

			if (heldItem == null) {
				invalidate();
				return;
			}

			intID = Item.getRawId(heldItem);

			IrisItemLightProvider lightProvider = (IrisItemLightProvider) heldItem;
			lightValue = lightProvider.getLightEmission(MinecraftClient.getInstance().player, heldStack);


			lightColor = lightProvider.getLightColor(MinecraftClient.getInstance().player, heldStack);
		}

		public int getIntID() {
			return intID;
		}

		public int getLightValue() {
			return lightValue;
		}

		public Vector3f getLightColor() {
			return lightColor;
		}
	}
}
