package dev.vexor.radium.mixin.extra.render.entity;

import dev.vexor.radium.extra.client.SodiumExtraClientMod;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.entity.decoration.ItemFrameEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameEntityRenderer.class)
public abstract class MixinItemFrameEntityRenderer extends EntityRenderer<ItemFrameEntity> {

    protected MixinItemFrameEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    @Inject(method = "render(Lnet/minecraft/entity/decoration/ItemFrameEntity;DDDFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockModelRenderer;render(Lnet/minecraft/client/render/model/BakedModel;FFFF)V", shift = At.Shift.AFTER), cancellable = true)
    public void render(ItemFrameEntity itemFrameEntity, double d, double e, double f, float g, float h, CallbackInfo ci) {
        if (!SodiumExtraClientMod.options().renderSettings.itemFrame) {
            ci.cancel();
        }
    }

    @Override
    protected boolean hasLabel(ItemFrameEntity entity) {
        if (!SodiumExtraClientMod.options().renderSettings.itemFrameNameTag) {
            return false;
        }
        return super.hasLabel(entity);
    }
}
