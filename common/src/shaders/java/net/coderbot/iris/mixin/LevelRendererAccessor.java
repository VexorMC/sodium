package net.coderbot.iris.mixin;
import net.minecraft.client.render.CameraView;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldRenderer.class)
public interface LevelRendererAccessor {
	@Invoker("renderLayer")
	int invokeRenderChunkLayer(RenderLayer renderLayer, double tickDelta, int anaglyphFilter, Entity entity);

	@Invoker("setupTerrain")
	void invokeSetupRender(Entity entity, double tickDelta, CameraView cameraView, int frame, boolean spectator);
}
