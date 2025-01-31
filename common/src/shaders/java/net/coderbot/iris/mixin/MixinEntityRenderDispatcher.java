package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {
    @Inject(method = "shouldRenderShadows", at = @At("RETURN"), cancellable = true)
	private void iris$shouldRenderShadows(CallbackInfoReturnable<Boolean> cir) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null && pipeline.shouldDisableVanillaEntityShadows()) {
			cir.setReturnValue(false);
		}
	}
}
