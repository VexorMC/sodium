package dev.vexor.radium.mixin.core.culling;

import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;

import dev.vexor.radium.culling.RadiumEntityCulling;
import dev.vexor.radium.culling.access.Cullable;
import net.minecraft.entity.Entity;

@Mixin(value = { Entity.class, BlockEntity.class })
public class CullableMixin implements Cullable {

    private long lasttime = 0;
    private boolean culled = false;
    private boolean outOfCamera = false;

    @Override
    public void setTimeout() {
        lasttime = System.currentTimeMillis() + 1000;
    }

    @Override
    public boolean isForcedVisible() {
        return lasttime > System.currentTimeMillis();
    }

    @Override
    public void setCulled(boolean value) {
        this.culled = value;
        if (!value) {
            setTimeout();
        }
    }

    @Override
    public boolean isCulled() {
        return RadiumEntityCulling.enabled && culled;
    }

    @Override
    public void setOutOfCamera(boolean value) {
        this.outOfCamera = value;
    }

    @Override
    public boolean isOutOfCamera() {
        return RadiumEntityCulling.enabled && outOfCamera;
    }

}
