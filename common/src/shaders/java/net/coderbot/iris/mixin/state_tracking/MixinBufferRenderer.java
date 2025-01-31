package net.coderbot.iris.mixin.state_tracking;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BufferRenderer.class)
public class MixinBufferRenderer {
    @Inject(method = "draw", at = @At("HEAD"))
    private void iris$beforeDrawArrays(BufferBuilder builder, CallbackInfo ci) {
        Iris.getPipelineManager().getPipeline().ifPresent(WorldRenderingPipeline::syncProgram);
    }

}
