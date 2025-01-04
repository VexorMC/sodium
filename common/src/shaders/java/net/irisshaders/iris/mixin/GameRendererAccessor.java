package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {

	@Accessor
	boolean getRenderHand();


	@Invoker
	void invokeBobView(float tickDelta);

	@Invoker
	void invokeBobViewWhenHurt(float tickDelta);

	@Invoker
	float invokeGetFov(float tickDelta, boolean b);

	@Invoker("shouldRenderBlockOutline")
	boolean shouldRenderBlockOutlineA();
}
