package net.irisshaders.iris.pathways;

import com.mojang.blaze3d.platform.GlStateManager;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixin.GameRendererAccessor;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

public class HandRenderer {
	public static final HandRenderer INSTANCE = new HandRenderer();
	public static final float DEPTH = 0.125F;
	private boolean ACTIVE;
	private boolean renderingSolid;

	private void setupGlState(GameRenderer gameRenderer, float tickDelta) {
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        float f = 0.07F;

        Project.gluPerspective(((GameRendererAccessor) gameRenderer).invokeGetFov(tickDelta, false), (float)MinecraftClient.getInstance().width / (float)MinecraftClient.getInstance().height, 0.05F, MinecraftClient.getInstance().options.viewDistance * 2.0F);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();

		((GameRendererAccessor) gameRenderer).invokeBobViewWhenHurt(tickDelta);

		if (MinecraftClient.getInstance().options.bobView) {
			((GameRendererAccessor) gameRenderer).invokeBobView(tickDelta);
		}
	}

	private boolean canRender(GameRenderer gameRenderer) {
		return !(!((GameRendererAccessor) gameRenderer).getRenderHand()
			|| MinecraftClient.getInstance().options.hudHidden
			|| (MinecraftClient.getInstance().getCameraEntity() instanceof LivingEntity && ((LivingEntity) MinecraftClient.getInstance().getCameraEntity()).isSleeping())
			|| MinecraftClient.getInstance().player.isSpectator());
	}

	public boolean isHandTranslucent() {
		Item item = MinecraftClient.getInstance().player.getStackInHand().getItem();

		if (item instanceof BlockItem) {
			return ((BlockItem) item).getBlock().getRenderLayerType() == RenderLayer.TRANSLUCENT;
		}

		return false;
	}

	public void renderSolid(float tickDelta, GameRenderer gameRenderer, WorldRenderingPipeline pipeline) {
		if (!canRender(gameRenderer) || !Iris.isPackInUseQuick()) {
			return;
		}

		ACTIVE = true;

        GL11.glPushMatrix();

        setupGlState(gameRenderer, tickDelta);

		pipeline.setPhase(WorldRenderingPhase.HAND_SOLID);

		MinecraftClient.getInstance().profiler.push("iris_hand");

		renderingSolid = true;

		gameRenderer.firstPersonRenderer.renderArmHoldingItem(tickDelta);

		MinecraftClient.getInstance().profiler.pop();

        GL11.glPopMatrix();

		renderingSolid = false;

		pipeline.setPhase(WorldRenderingPhase.NONE);

		ACTIVE = false;
	}

	public void renderTranslucent9(float tickDelta, GameRenderer gameRenderer, WorldRenderingPipeline pipeline) {
		if (!canRender(gameRenderer) || !isHandTranslucent() || !Iris.isPackInUseQuick()) {
			return;
		}

		ACTIVE = true;

		pipeline.setPhase(WorldRenderingPhase.HAND_TRANSLUCENT);

        GL11.glPushMatrix();

		setupGlState(gameRenderer, tickDelta);

        MinecraftClient.getInstance().profiler.push("iris_hand_translucent");

        gameRenderer.firstPersonRenderer.renderArmHoldingItem(tickDelta);

        MinecraftClient.getInstance().profiler.pop();

        GL11.glPopMatrix();

		pipeline.setPhase(WorldRenderingPhase.NONE);

		ACTIVE = false;
	}

	public boolean isActive() {
		return ACTIVE;
	}

	public boolean isRenderingSolid() {
		return renderingSolid;
	}
}
