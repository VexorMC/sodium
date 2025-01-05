package net.irisshaders.iris.api.v0.item;

import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import org.lwjgl.util.vector.Vector3f;

public interface IrisItemLightProvider {

	Vector3f DEFAULT_LIGHT_COLOR = new Vector3f(1, 1, 1);

	default int getLightEmission(ClientPlayerEntity player, ItemStack stack) {
		if (stack.getItem() instanceof BlockItem item) {

			return item.getBlock().getLightLevel();
		}

		return 0;
	}

	default Vector3f getLightColor(ClientPlayerEntity player, ItemStack stack) {
		return DEFAULT_LIGHT_COLOR;
	}
}
