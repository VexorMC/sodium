package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.rendertarget.NativeImageBackedCustomTexture;
import net.coderbot.iris.shaderpack.texture.CustomTextureData;
import net.coderbot.iris.shaderpack.texture.TextureFilteringData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

/**
 * This Mixin is responsible for registering the "widgets" texture used in Iris' GUI's.
 * Normally Fabric API would do this automatically, but we don't use it here, so it must be done manually.
 */
@Mixin(MinecraftClient.class)
public class MixinMinecraft_Images {
	@Inject(method = "initializeGame", at = @At("TAIL"))
	private void iris$setupImages(CallbackInfo ci) {
		try {
			MinecraftClient.getInstance().getTextureManager().loadTexture(new Identifier("iris", "textures/gui/widgets.png"), new NativeImageBackedCustomTexture(new CustomTextureData.PngData(new TextureFilteringData(false, false), IOUtils.toByteArray(Iris.class.getResourceAsStream("/assets/iris/textures/gui/widgets.png")))));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
