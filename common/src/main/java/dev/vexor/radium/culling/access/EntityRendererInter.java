package dev.vexor.radium.culling.access;

import net.minecraft.entity.Entity;

public interface EntityRendererInter<T extends Entity> {

	boolean shadowShouldShowName(T entity);

	void shadowRenderNameTag(T entity, double offsetX, double offsetY, double offsetZ);

}