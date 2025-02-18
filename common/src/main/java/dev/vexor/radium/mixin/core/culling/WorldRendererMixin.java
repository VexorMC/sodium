package dev.vexor.radium.mixin.core.culling;

import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.vexor.radium.culling.RadiumEntityCulling;
import dev.vexor.radium.culling.access.Cullable;
import dev.vexor.radium.culling.access.EntityRendererInter;
import net.minecraft.entity.Entity;

@Mixin(EntityRenderDispatcher.class)
public abstract class WorldRendererMixin {
    @Shadow
    public abstract <T extends Entity> EntityRenderer<T> getRenderer(Entity entity);

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    public void doRenderEntity(Entity entity, double d, double e, double f, float g, float h, CallbackInfoReturnable<Boolean> cir) {
        Cullable cullable = (Cullable) entity;
        if (!cullable.isForcedVisible() && cullable.isCulled()) {
            EntityRendererInter<Entity> entityRenderer = (EntityRendererInter) this.getRenderer(entity);
            if (SodiumClientMod.options().culling.renderNametagsThroughWalls && entityRenderer.shadowShouldShowName(entity)) {
                entityRenderer.shadowRenderNameTag(entity, d, e, f);
                //entityRenderer.doRender(entity, entity.posX, entity.posY, entity.posZ, tickDelta, tickDelta);
            }
            RadiumEntityCulling.INSTANCE.skippedEntities++;
            cir.cancel();
            return;
        }
        RadiumEntityCulling.INSTANCE.renderedEntities++;
        cullable.setOutOfCamera(false);
    }

}
