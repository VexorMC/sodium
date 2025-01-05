package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
@Environment(EnvType.CLIENT)
public class MixinMinecraft_PipelineManagement {
    @Shadow
    public ClientWorld world;

    /**
	 * Should run before the Minecraft.level field is updated after disconnecting from a server or leaving a singleplayer world
     * Should run before the Minecraft.level field is updated after receiving a login or respawn packet
     * NB: Not on leave, another inject is used for that
	 */
	@Inject(method = "connect(Lnet/minecraft/client/world/ClientWorld;)V", at = @At("HEAD"))
	public void iris$trackLastDimensionOnLeave(ClientWorld world, CallbackInfo ci) {
		Iris.lastDimension = Iris.getCurrentDimension();

        if (Iris.getCurrentDimension() != Iris.lastDimension) {
            Iris.logger.info("Reloading pipeline on dimension change: " + Iris.lastDimension + " => " + Iris.getCurrentDimension());
            // Destroy pipelines when changing dimensions.
            Iris.getPipelineManager().destroyPipeline();

            // NB: We need create the pipeline immediately, so that it is ready by the time that Sodium starts trying to
            // initialize its world renderer.
            if (world != null) {
                Iris.getPipelineManager().preparePipeline(Iris.getCurrentDimension());
            }
        }
	}
}
