package net.coderbot.iris.mixin.texture;

import net.coderbot.iris.texture.TextureTracker;
import net.minecraft.client.texture.AbstractTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractTexture.class)
public class MixinAbstractTexture {
	@Shadow
	protected int glId;

	// Inject after the newly-generated texture ID has been stored into the id field
	@Inject(method = "getGlId", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/TextureUtil;getTexLevelParameter()I", shift = Shift.BY, by = 2))
	private void iris$afterGenerateId(CallbackInfoReturnable<Integer> cir) {
		TextureTracker.INSTANCE.trackTexture(glId, (AbstractTexture) (Object) this);
	}
}
