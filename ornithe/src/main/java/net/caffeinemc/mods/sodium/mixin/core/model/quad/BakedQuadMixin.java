package net.caffeinemc.mods.sodium.mixin.core.model.quad;

import net.caffeinemc.mods.sodium.client.model.quad.BakedQuadView;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFlags;
import net.caffeinemc.mods.sodium.client.util.ModelQuadUtil;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.TexturedBakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BakedQuad.class)
public abstract class BakedQuadMixin implements BakedQuadView {
    @Shadow
    @Final
    protected Direction direction; // This is really the light face, but we can't rename it.

    @Shadow
    @Final
    protected int[] vertexData;
    @Shadow
    @Final
    protected int colorIndex;

    @Unique
    private int flags;

    @Unique
    private int normal;

    @Unique
    private ModelQuadFacing normalFace = null;

    @Shadow
    public abstract boolean hasColor();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(int[] is, int i, Direction direction, CallbackInfo ci) {
        this.normal = this.calculateNormal();
        this.normalFace = ModelQuadFacing.fromPackedNormal(this.normal);

        this.flags = ModelQuadFlags.getQuadFlags(this, direction);
    }

    @Override
    public float getX(int idx) {
        return Float.intBitsToFloat(this.vertexData[ModelQuadUtil.vertexOffset(idx) + ModelQuadUtil.POSITION_INDEX]);
    }

    @Override
    public float getY(int idx) {
        return Float.intBitsToFloat(this.vertexData[ModelQuadUtil.vertexOffset(idx) + ModelQuadUtil.POSITION_INDEX + 1]);
    }

    @Override
    public float getZ(int idx) {
        return Float.intBitsToFloat(this.vertexData[ModelQuadUtil.vertexOffset(idx) + ModelQuadUtil.POSITION_INDEX + 2]);
    }

    @Override
    public int getColor(int idx) {
        return this.vertexData[ModelQuadUtil.vertexOffset(idx) + ModelQuadUtil.COLOR_INDEX];
    }

    @Override
    public int getVertexNormal(int idx) {
        return this.vertexData[ModelQuadUtil.vertexOffset(idx) + ModelQuadUtil.NORMAL_INDEX];
    }

    @Override
    public int getLight(int idx) {
        return this.vertexData[ModelQuadUtil.vertexOffset(idx) + ModelQuadUtil.LIGHT_INDEX];
    }

    @Override
    public Sprite getSprite() {
        if (((Object)this) instanceof TexturedBakedQuad bq) {
            return bq.sprite;
        }
        return null;
    }

    @Override
    public float getTexU(int idx) {
        return Float.intBitsToFloat(this.vertexData[ModelQuadUtil.vertexOffset(idx) + ModelQuadUtil.TEXTURE_INDEX]);
    }

    @Override
    public float getTexV(int idx) {
        return Float.intBitsToFloat(this.vertexData[ModelQuadUtil.vertexOffset(idx) + ModelQuadUtil.TEXTURE_INDEX + 1]);
    }

    @Override
    public int getFlags() {
        return this.flags;
    }

    @Override
    public int getTintIndex() {
        return this.colorIndex;
    }

    @Override
    public ModelQuadFacing getNormalFace() {
        return this.normalFace;
    }

    @Override
    public int getFaceNormal() {
        return this.normal;
    }

    @Override
    public Direction getLightFace() {
        return this.direction;
    }


    @Override
    public int getMaxLightQuad(int idx) {
        return this.vertexData[ModelQuadUtil.vertexOffset(idx) + ModelQuadUtil.LIGHT_INDEX];
    }

    @Override
    @Unique(silent = true) // The target class has a function with the same name in a remapped environment
    public boolean hasShade() {
        return this.hasColor();
    }

    @Override
    public boolean hasAO() {
        return true;
    }
}
