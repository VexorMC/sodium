package dev.vexor.radium.mixin.core.culling;

import net.minecraft.client.render.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import dev.vexor.radium.culling.access.EntityRendererInter;
import net.minecraft.entity.Entity;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> implements EntityRendererInter<T>  {

    @Shadow
    protected abstract void method_10208(T entity, double d, double e, double f);

    @Shadow
    protected abstract boolean hasLabel(T entity);

    @Override
    public boolean shadowShouldShowName(T entity) {
        return this.hasLabel(entity);
    }

    @Override
    public void shadowRenderNameTag(T entity, double offsetX, double offsetY, double offsetZ) {
        this.method_10208(entity, offsetX, offsetY, offsetZ);
    }
}
