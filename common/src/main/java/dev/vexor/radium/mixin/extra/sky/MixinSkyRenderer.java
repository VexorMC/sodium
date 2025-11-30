package dev.vexor.radium.mixin.extra.sky;

import com.mojang.blaze3d.platform.GlStateManager;
import dev.vexor.radium.extra.client.SodiumExtraClientMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class MixinSkyRenderer {
    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    protected abstract void renderEndSky();

    @Shadow
    private ClientWorld world;

    @Shadow
    private VertexBuffer lightSkyBuffer;

    @Shadow
    private boolean vbo;

    @Shadow
    private int lightSkyList;

    @Shadow
    @Final
    private TextureManager textureManager;

    @Shadow
    @Final
    private static Identifier SUN;

    @Shadow
    @Final
    private static Identifier MOON_PHASES;

    @Shadow
    private VertexBuffer starsBuffer;

    @Shadow
    private int starsList;

    @Shadow
    private VertexBuffer darkSkyBuffer;

    @Shadow
    private int darkSkyList;

    /**
     * @reason Sky Settings
     * @author Lunasa
     */
    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    public void renderSky(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
        if (!SodiumExtraClientMod.options().detailSettings.sky) {
            ci.cancel();
        }
    }
}
