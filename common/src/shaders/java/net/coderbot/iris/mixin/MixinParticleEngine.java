package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPhase;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ensures that all particles are rendered with the textured_lit shader program.
 */
@Mixin(ParticleManager.class)
public class MixinParticleEngine {
	@Inject(method = "renderParticles", at = @At("HEAD"))
	private void iris$beginDrawingParticles(Entity entity, float tickDelta, CallbackInfo ci) {
		Iris.getPipelineManager().getPipeline().ifPresent(pipeline -> pipeline.setPhase(WorldRenderingPhase.PARTICLES));
	}

	@Inject(method = "renderParticles", at = @At("RETURN"))
	private void iris$finishDrawingParticles(Entity entity, float tickDelta, CallbackInfo ci) {
		Iris.getPipelineManager().getPipeline().ifPresent(pipeline -> pipeline.setPhase(WorldRenderingPhase.NONE));
	}
}
