package net.irisshaders.iris.mixin;

import dev.vexor.radium.compat.mojang.math.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.render.CameraView;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.SortedSet;

@Mixin(WorldRenderer.class)
public interface LevelRendererAccessor {
	@Accessor("entityRenderDispatcher")
	EntityRenderDispatcher getEntityRenderDispatcher();

	@Invoker("renderLayer")
	int invokeRenderSectionLayer(RenderLayer renderLayer, double tickDelta, int anaglyphFilter, Entity entity);

	@Invoker("setupTerrain")
	void invokeSetupRender(Entity entity, double tickDelta, CameraView cameraView, int frame, boolean spectator);

	@Invoker("renderEntity")
	void invokeRenderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, PoseStack poseStack, MultiBufferSource bufferSource);

	@Accessor("world")
    ClientWorld getLevel();

	@Accessor("renderBuffers")
	RenderBuffers getRenderBuffers();

	@Accessor("renderBuffers")
	void setRenderBuffers(RenderBuffers buffers);

	@Accessor("generateClouds")
	boolean shouldRegenerateClouds();

	@Accessor("generateClouds")
	void setShouldRegenerateClouds(boolean shouldRegenerate);

	@Invoker
	boolean invokeDoesMobEffectBlockSky(Camera mainCamera);

	@Accessor
	Long2ObjectMap<SortedSet<BlockDestructionProgress>> getDestructionProgress();
}
