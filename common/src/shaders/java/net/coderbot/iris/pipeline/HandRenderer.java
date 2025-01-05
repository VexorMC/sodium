package net.coderbot.iris.pipeline;

import net.coderbot.iris.mixin.GameRendererAccessor;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class HandRenderer {
	public static final HandRenderer INSTANCE = new HandRenderer();

	private boolean ACTIVE;
	private boolean renderingSolid;

	public static final float DEPTH = 0.125F;

	private void setupGlState(GameRenderer gameRenderer, float tickDelta) {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        // We need to scale the matrix by 0.125 so the hand doesn't clip through blocks.
        GL11.glScalef(1.0F, 1.0F, DEPTH);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

		((GameRendererAccessor) gameRenderer).invokeBobViewWhenHurt(tickDelta);

		if (MinecraftClient.getInstance().options.bobView) {
			((GameRendererAccessor) gameRenderer).invokeBobView(tickDelta);
		}
	}

	private boolean canRender(GameRenderer gameRenderer) {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.options.perspective == 0 &&
                !client.player.isSleeping() &&
                !client.options.hudHidden;
    }

	public boolean isHandTranslucent() {
        ItemStack stack = MinecraftClient.getInstance().player.getMainHandStack();

        if (stack == null) return false;

		Item item = stack.getItem();

        if (item instanceof BlockItem blockItem) {
            return blockItem.getBlock().getRenderLayerType() == RenderLayer.TRANSLUCENT;
        }

		return false;
	}


	public void renderSolid(float tickDelta, GameRenderer gameRenderer, WorldRenderingPipeline pipeline) {
		if (!canRender(gameRenderer)) {
			return;
		}

		ACTIVE = true;

		pipeline.setPhase(WorldRenderingPhase.HAND_SOLID);

        GL11.glPushMatrix();

		MinecraftClient.getInstance().profiler.push("iris_hand");

		setupGlState(gameRenderer, tickDelta);

		renderingSolid = true;

        MinecraftClient.getInstance().gameRenderer.enableLightmap();
        MinecraftClient.getInstance().getHeldItemRenderer().renderArmHoldingItem(tickDelta);
        MinecraftClient.getInstance().gameRenderer.disableLightmap();

        MinecraftClient.getInstance().profiler.pop();

        GL11.glPopMatrix();

        resetProjectionMatrix();

		renderingSolid = false;

		pipeline.setPhase(WorldRenderingPhase.NONE);

		ACTIVE = false;
	}

	public void renderTranslucent(float tickDelta, GameRenderer gameRenderer, WorldRenderingPipeline pipeline) {
		if (!canRender(gameRenderer) || !isHandTranslucent()) {
			return;
		}

		ACTIVE = true;

		pipeline.setPhase(WorldRenderingPhase.HAND_TRANSLUCENT);

        MinecraftClient.getInstance().profiler.push("iris_hand_translucent");

        GL11.glPushMatrix();

		setupGlState(gameRenderer, tickDelta);

        MinecraftClient.getInstance().gameRenderer.enableLightmap();
        MinecraftClient.getInstance().getHeldItemRenderer().renderArmHoldingItem(tickDelta);
        MinecraftClient.getInstance().gameRenderer.disableLightmap();

        MinecraftClient.getInstance().profiler.pop();

        GL11.glPopMatrix();

        resetProjectionMatrix();

		pipeline.setPhase(WorldRenderingPhase.NONE);

		ACTIVE = false;
	}

	public boolean isActive() {
		return ACTIVE;
	}

	public boolean isRenderingSolid() {
		return renderingSolid;
	}

    private void resetProjectionMatrix() {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glMultMatrixf(CapturedRenderingState.INSTANCE.getGbufferProjection().get(new float[16]));
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }
}
