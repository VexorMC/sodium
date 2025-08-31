package dev.vexor.radium.mixin.core;

import net.caffeinemc.mods.sodium.client.util.frustum.ExtendedFrustum;
import net.minecraft.client.render.BaseFrustum;
import net.minecraft.client.render.CullingCameraView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CullingCameraView.class)
public class MixinCullingCameraView implements ExtendedFrustum {
    @Shadow
    private BaseFrustum clipper;

    @Override
    public int radium$intersect(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return ((ExtendedFrustum)this.clipper).radium$intersect(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
