package dev.vexor.radium.mixin.core.culling;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.vexor.radium.culling.RadiumEntityCulling;
import dev.vexor.radium.culling.access.Cullable;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
    
    @Inject(method = "renderEntity(Lnet/minecraft/block/entity/BlockEntity;DDDFI)V", at = @At("HEAD"), cancellable = true)
    public void renderTileEntityAt(BlockEntity entity, double x, double y, double z, float tickDelta, int destroyProgress, CallbackInfo ci) {
        if (!((Cullable) entity).isForcedVisible() && ((Cullable) entity).isCulled()) {
            RadiumEntityCulling.INSTANCE.skippedBlockEntities++;
            ci.cancel();
            return;
        }
        RadiumEntityCulling.INSTANCE.renderedBlockEntities++;
    }

}
