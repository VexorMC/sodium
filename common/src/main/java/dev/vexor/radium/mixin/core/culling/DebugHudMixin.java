package dev.vexor.radium.mixin.core.culling;

import java.util.List;

import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.vexor.radium.culling.RadiumEntityCulling;

@Mixin(DebugHud.class)
public class DebugHudMixin {

    public DebugHudMixin() {
        RadiumEntityCulling.INSTANCE.clientTick();
    }
    
    @Inject(method = "getLeftText", at = @At("RETURN"), cancellable = true)
    public void getLeftText(CallbackInfoReturnable<List<String>> callback) {
        List<String> list = callback.getReturnValue();
        list.add("[Culling] Last pass: " + RadiumEntityCulling.INSTANCE.cullTask.lastTime + "ms");
        list.add("[Culling] Rendered Block Entities: " + RadiumEntityCulling.INSTANCE.renderedBlockEntities + " Skipped: " + RadiumEntityCulling.INSTANCE.skippedBlockEntities);
        list.add("[Culling] Rendered Entities: " + RadiumEntityCulling.INSTANCE.renderedEntities + " Skipped: " + RadiumEntityCulling.INSTANCE.skippedEntities);
        //list.add("[Culling] Ticked Entities: " + lastTickedEntities + " Skipped: " + lastSkippedEntityTicks);
        
        RadiumEntityCulling.INSTANCE.renderedBlockEntities = 0;
        RadiumEntityCulling.INSTANCE.skippedBlockEntities = 0;
        RadiumEntityCulling.INSTANCE.renderedEntities = 0;
        RadiumEntityCulling.INSTANCE.skippedEntities = 0;

        callback.setReturnValue(list);
    }
    
}
