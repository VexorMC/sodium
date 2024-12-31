package net.irisshaders.iris.mixin;

import dev.vexor.radium.compat.mojang.math.PoseStack;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
	@Accessor
	boolean getRenderHand();

	@Invoker("shouldRenderBlockOutline")
	boolean shouldRenderBlockOutlineA();
}
