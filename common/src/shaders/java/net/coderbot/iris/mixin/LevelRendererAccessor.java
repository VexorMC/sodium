package net.coderbot.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.render.CameraView;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldRenderer.class)
public interface LevelRendererAccessor {
	@Accessor("entityRenderDispatcher")
    EntityRenderDispatcher getEntityRenderDispatcher();

	@Invoker("renderLayer")
    int invokeRenderLayer(RenderLayer renderLayer, double tickDelta, int anaglyphFilter, Entity entity);

	@Invoker("setupTerrain")
	void invokeSetupRender(Entity entity, double tickDelta, CameraView cameraView, int frame, boolean spectator);

	@Accessor("world")
    ClientWorld getLevel();

	@Accessor("ticks")
	int getFrameId();

	@Accessor("ticks")
	void setFrameId(int frame);
}
