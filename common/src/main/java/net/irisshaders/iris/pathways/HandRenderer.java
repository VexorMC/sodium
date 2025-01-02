package net.irisshaders.iris.pathways;

import dev.vexor.radium.compat.mojang.math.PoseStack;
import net.irisshaders.batchedentityrendering.impl.FullyBufferedMultiBufferSource;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixin.GameRendererAccessor;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public class HandRenderer {
	public static final HandRenderer INSTANCE = new HandRenderer();
	public static final float DEPTH = 0.125F;
	private final FullyBufferedMultiBufferSource bufferSource = new FullyBufferedMultiBufferSource();
	private boolean ACTIVE;
	private boolean renderingSolid;

	private PoseStack setupGlState(GameRenderer gameRenderer, Matrix4fc modelMatrix, float tickDelta) {
		final PoseStack poseStack = new PoseStack();

		// We need to scale the matrix by 0.125 so the hand doesn't clip through blocks.
		Matrix4f scaleMatrix = new Matrix4f().scale(1F, 1F, DEPTH);
		scaleMatrix.mul(gameRenderer.getProjectionMatrix(((GameRendererAccessor) gameRenderer).invokeGetFov(camera, tickDelta, false)));
		gameRenderer.resetProjectionMatrix(scaleMatrix);

		poseStack.setIdentity();

		((GameRendererAccessor) gameRenderer).invokeBobHurt(poseStack, tickDelta);

		if (MinecraftClient.getInstance().options.bobView().get()) {
			((GameRendererAccessor) gameRenderer).invokeBobView(poseStack, tickDelta);
		}

		return poseStack;
	}

	private boolean canRender(Camera camera, GameRenderer gameRenderer) {
		return !(!((GameRendererAccessor) gameRenderer).getRenderHand()
			|| camera.isDetached()
			|| !(camera.getEntity() instanceof Player)
			|| ((GameRendererAccessor) gameRenderer).getPanoramicMode()
			|| MinecraftClient.getInstance().options.hideGui
			|| (camera.getEntity() instanceof LivingEntity && ((LivingEntity) camera.getEntity()).isSleeping())
			|| MinecraftClient.getInstance().gameMode.getPlayerMode() == GameType.SPECTATOR);
	}

	public boolean isHandTranslucent(InteractionHand hand) {
		Item item = MinecraftClient.getInstance().player.getItemBySlot(hand == InteractionHand.OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND).getItem();

		if (item instanceof BlockItem) {
			return ItemBlockRenderTypes.getChunkRenderType(((BlockItem) item).getBlock().defaultBlockState()) == RenderType.translucent();
		}

		return false;
	}

	public boolean isAnyHandTranslucent() {
		return isHandTranslucent(InteractionHand.MAIN_HAND) || isHandTranslucent(InteractionHand.OFF_HAND);
	}

	public void renderSolid(Matrix4fc modelMatrix, float tickDelta, Camera camera, GameRenderer gameRenderer, WorldRenderingPipeline pipeline) {
		if (!canRender(camera, gameRenderer) || !Iris.isPackInUseQuick()) {
			return;
		}

		ACTIVE = true;

		PoseStack poseStack = setupGlState(gameRenderer, camera, modelMatrix, tickDelta);

		pipeline.setPhase(WorldRenderingPhase.HAND_SOLID);

		poseStack.pushPose();

		MinecraftClient.getInstance().getProfiler().push("iris_hand");

		renderingSolid = true;

		RenderSystem.getModelViewStack().pushMatrix();
		RenderSystem.getModelViewStack().set(poseStack.last().pose());
		RenderSystem.applyModelViewMatrix();

		gameRenderer.itemInHandRenderer.renderHandsWithItems(tickDelta, new PoseStack(), bufferSource.getUnflushableWrapper(), MinecraftClient.getInstance().player, MinecraftClient.getInstance().getEntityRenderDispatcher().getPackedLightCoords(camera.getEntity(), tickDelta));

		MinecraftClient.getInstance().getProfiler().pop();

		bufferSource.readyUp();
		bufferSource.endBatch();

		gameRenderer.resetProjectionMatrix(new Matrix4f(CapturedRenderingState.INSTANCE.getGbufferProjection()));

		poseStack.popPose();
		RenderSystem.getModelViewStack().popMatrix();
		RenderSystem.applyModelViewMatrix();

		renderingSolid = false;

		pipeline.setPhase(WorldRenderingPhase.NONE);

		ACTIVE = false;
	}

	public void renderTranslucent(Matrix4fc modelMatrix, float tickDelta, Camera camera, GameRenderer gameRenderer, WorldRenderingPipeline pipeline) {
		if (!canRender(camera, gameRenderer) || !isAnyHandTranslucent() || !Iris.isPackInUseQuick()) {
			return;
		}

		ACTIVE = true;

		pipeline.setPhase(WorldRenderingPhase.HAND_TRANSLUCENT);

		PoseStack poseStack = setupGlState(gameRenderer, camera, modelMatrix, tickDelta);

		poseStack.pushPose();

		MinecraftClient.getInstance().getProfiler().push("iris_hand_translucent");

		RenderSystem.getModelViewStack().pushMatrix();
		RenderSystem.getModelViewStack().set(poseStack.last().pose());
		RenderSystem.applyModelViewMatrix();

		gameRenderer.itemInHandRenderer.renderHandsWithItems(tickDelta, new PoseStack(), bufferSource, MinecraftClient.getInstance().player, MinecraftClient.getInstance().getEntityRenderDispatcher().getPackedLightCoords(camera.getEntity(), tickDelta));

		poseStack.popPose();

		MinecraftClient.getInstance().getProfiler().pop();

		gameRenderer.resetProjectionMatrix(new Matrix4f(CapturedRenderingState.INSTANCE.getGbufferProjection()));

		bufferSource.endBatch();
		RenderSystem.getModelViewStack().popMatrix();
		RenderSystem.applyModelViewMatrix();

		pipeline.setPhase(WorldRenderingPhase.NONE);

		ACTIVE = false;
	}

	public boolean isActive() {
		return ACTIVE;
	}

	public boolean isRenderingSolid() {
		return renderingSolid;
	}

	public FullyBufferedMultiBufferSource getBufferSource() {
		return bufferSource;
	}
}
