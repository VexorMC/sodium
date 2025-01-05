package net.coderbot.iris.mixin.fantastic;

import net.minecraft.client.particle.BarrierParticle;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BarrierParticle.class)
public class MixinBarrierParticle {
	@Unique
	private boolean isOpaque;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void iris$resolveTranslucency(World world, double d, double e, double f, Item item, CallbackInfo ci) {
		if (item instanceof BlockItem blockItem) {
			RenderLayer type = blockItem.getBlock().getRenderLayerType();

			if (type == RenderLayer.SOLID || type == RenderLayer.CUTOUT || type == RenderLayer.CUTOUT_MIPPED) {
				isOpaque = true;
			}
		}
	}
}
