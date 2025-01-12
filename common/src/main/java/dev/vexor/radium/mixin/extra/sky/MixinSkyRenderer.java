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
    @Overwrite
    public void renderSky(float tickDelta, int anaglyphFilter) {
        if (!SodiumExtraClientMod.options().detailSettings.sky) {
            return;
        }
        if (this.client.world.dimension.getType() == 1) {
            this.renderEndSky();
        } else if (this.client.world.dimension.canPlayersSleep()) {
            GlStateManager.disableTexture();
            Vec3d vec3d = this.world.method_3631(this.client.getCameraEntity(), tickDelta);
            float f = (float)vec3d.x;
            float g = (float)vec3d.y;
            float h = (float)vec3d.z;
            if (anaglyphFilter != 2) {
                float i = (f * 30.0F + g * 59.0F + h * 11.0F) / 100.0F;
                float j = (f * 30.0F + g * 70.0F) / 100.0F;
                float k = (f * 30.0F + h * 70.0F) / 100.0F;
                f = i;
                g = j;
                h = k;
            }

            GlStateManager.color(f, g, h);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            GlStateManager.depthMask(false);
            GlStateManager.enableFog();
            GlStateManager.color(f, g, h);
            if (this.vbo) {
                this.lightSkyBuffer.bind();
                GL11.glEnableClientState(32884);
                GL11.glVertexPointer(3, 5126, 12, 0L);
                this.lightSkyBuffer.draw(7);
                this.lightSkyBuffer.unbind();
                GL11.glDisableClientState(32884);
            } else {
                GlStateManager.callList(this.lightSkyList);
            }

            GlStateManager.disableFog();
            GlStateManager.disableAlphaTest();
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(770, 771, 1, 0);
            DiffuseLighting.disable();
            float[] fs = this.world.dimension.getBackgroundColor(this.world.getSkyAngle(tickDelta), tickDelta);
            if (fs != null) {
                GlStateManager.disableTexture();
                GlStateManager.shadeModel(7425);
                GlStateManager.pushMatrix();
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(MathHelper.sin(this.world.getSkyAngleRadians(tickDelta)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
                float l = fs[0];
                float m = fs[1];
                float n = fs[2];
                if (anaglyphFilter != 2) {
                    float o = (l * 30.0F + m * 59.0F + n * 11.0F) / 100.0F;
                    float p = (l * 30.0F + m * 70.0F) / 100.0F;
                    float q = (l * 30.0F + n * 70.0F) / 100.0F;
                    l = o;
                    m = p;
                    n = q;
                }

                bufferBuilder.begin(6, VertexFormats.POSITION_COLOR);
                bufferBuilder.vertex((double)0.0F, (double)100.0F, (double)0.0F).color(l, m, n, fs[3]).next();
                int r = 16;

                for(int s = 0; s <= 16; ++s) {
                    float q = (float)s * (float)Math.PI * 2.0F / 16.0F;
                    float t = MathHelper.sin(q);
                    float u = MathHelper.cos(q);
                    bufferBuilder.vertex((double)(t * 120.0F), (double)(u * 120.0F), (double)(-u * 40.0F * fs[3])).color(fs[0], fs[1], fs[2], 0.0F).next();
                }

                tessellator.draw();
                GlStateManager.popMatrix();
                GlStateManager.shadeModel(7424);
            }

            GlStateManager.enableTexture();
            GlStateManager.blendFuncSeparate(770, 1, 1, 0);
            GlStateManager.pushMatrix();
            float l = 1.0F - this.world.getRainGradient(tickDelta);
            GlStateManager.color(1.0F, 1.0F, 1.0F, l);
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(this.world.getSkyAngle(tickDelta) * 360.0F, 1.0F, 0.0F, 0.0F);
            float m = 30.0F;

            if (SodiumExtraClientMod.options().detailSettings.sun) {
                this.textureManager.bindTexture(SUN);
                bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE);
                bufferBuilder.vertex((double)(-m), (double)100.0F, (double)(-m)).texture((double)0.0F, (double)0.0F).next();
                bufferBuilder.vertex((double)m, (double)100.0F, (double)(-m)).texture((double)1.0F, (double)0.0F).next();
                bufferBuilder.vertex((double)m, (double)100.0F, (double)m).texture((double)1.0F, (double)1.0F).next();
                bufferBuilder.vertex((double)(-m), (double)100.0F, (double)m).texture((double)0.0F, (double)1.0F).next();
                tessellator.draw();
            }

            m = 20.0F;

            this.textureManager.bindTexture(MOON_PHASES);
            int v = this.world.getMoonPhase();
            int r = v % 4;
            int s = v / 4 % 2;
            float q = (float)(r + 0) / 4.0F;
            float t = (float)(s + 0) / 2.0F;
            float u = (float)(r + 1) / 4.0F;
            float w = (float)(s + 1) / 2.0F;
            if (SodiumExtraClientMod.options().detailSettings.moon) {
                bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE);
                bufferBuilder.vertex((double) (-m), (double) -100.0F, (double) m).texture((double) u, (double) w).next();
                bufferBuilder.vertex((double) m, (double) -100.0F, (double) m).texture((double) q, (double) w).next();
                bufferBuilder.vertex((double) m, (double) -100.0F, (double) (-m)).texture((double) q, (double) t).next();
                bufferBuilder.vertex((double) (-m), (double) -100.0F, (double) (-m)).texture((double) u, (double) t).next();
                tessellator.draw();
            }
            GlStateManager.disableTexture();
            float x = this.world.method_3707(tickDelta) * l;
            if (x > 0.0F && SodiumExtraClientMod.options().detailSettings.stars) {
                GlStateManager.color(x, x, x, x);
                if (this.vbo) {
                    this.starsBuffer.bind();
                    GL11.glEnableClientState(32884);
                    GL11.glVertexPointer(3, 5126, 12, 0L);
                    this.starsBuffer.draw(7);
                    this.starsBuffer.unbind();
                    GL11.glDisableClientState(32884);
                } else {
                    GlStateManager.callList(this.starsList);
                }
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.enableAlphaTest();
            GlStateManager.enableFog();
            GlStateManager.popMatrix();
            GlStateManager.disableTexture();
            GlStateManager.color(0.0F, 0.0F, 0.0F);
            double d = this.client.player.getCameraPosVec(tickDelta).y - this.world.getHorizonHeight();
            if (d < (double)0.0F) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(0.0F, 12.0F, 0.0F);
                if (this.vbo) {
                    this.darkSkyBuffer.bind();
                    GL11.glEnableClientState(32884);
                    GL11.glVertexPointer(3, 5126, 12, 0L);
                    this.darkSkyBuffer.draw(7);
                    this.darkSkyBuffer.unbind();
                    GL11.glDisableClientState(32884);
                } else {
                    GlStateManager.callList(this.darkSkyList);
                }

                GlStateManager.popMatrix();
                float n = 1.0F;
                float o = -((float)(d + (double)65.0F));
                float p = -1.0F;
                bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
                bufferBuilder.vertex(-1.0F, o, 1.0F).color(0, 0, 0, 255).next();
                bufferBuilder.vertex(1.0F, o, 1.0F).color(0, 0, 0, 255).next();
                bufferBuilder.vertex(1.0F, -1.0F, 1.0F).color(0, 0, 0, 255).next();
                bufferBuilder.vertex(-1.0F, -1.0F, 1.0F).color(0, 0, 0, 255).next();
                bufferBuilder.vertex(-1.0F, -1.0F, -1.0F).color(0, 0, 0, 255).next();
                bufferBuilder.vertex(1.0F, -1.0F, -1.0F).color(0, 0, 0, 255).next();
                bufferBuilder.vertex(1.0F, o, -1.0F).color(0, 0, 0, 255).next();
                bufferBuilder.vertex(-1.0F, o, -1.0F).color(0, 0, 0, 255).next();
                bufferBuilder.vertex(1.0F, -1.0F, -1.0F).color(0, 0, 0, 255).next();
                bufferBuilder.vertex(1.0F, -1.0F, 1.0F).color(0, 0, 0, 255).next();
                bufferBuilder.vertex(1.0F, o, 1.0F).color(0, 0, 0, 255).next();
                bufferBuilder.vertex(1.0F, o, -1.0F).color(0, 0, 0, 255).next();
                bufferBuilder.vertex(-1.0F, o, -1.0F).color(0, 0, 0, 255).next();
                bufferBuilder.vertex(-1.0F, o, 1.0F).color(0, 0, 0, 255).next();
                bufferBuilder.vertex(-1.0F, -1.0F, 1.0F).color(0, 0, 0, 255).next();
                bufferBuilder.vertex(-1.0F, -1.0F, -1.0F).color(0, 0, 0, 255).next();
                bufferBuilder.vertex(-1.0F, -1.0F, -1.0F).color(0, 0, 0, 255).next();
                bufferBuilder.vertex(-1.0F, -1.0F, 1.0F).color(0, 0, 0, 255).next();
                bufferBuilder.vertex(1.0F, -1.0F, 1.0F).color(0, 0, 0, 255).next();
                bufferBuilder.vertex(1.0F, -1.0F, -1.0F).color(0, 0, 0, 255).next();
                tessellator.draw();
            }

            if (this.world.dimension.hasGround()) {
                GlStateManager.color(f * 0.2F + 0.04F, g * 0.2F + 0.04F, h * 0.6F + 0.1F);
            } else {
                GlStateManager.color(f, g, h);
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, -((float)(d - (double)16.0F)), 0.0F);
            GlStateManager.callList(this.darkSkyList);
            GlStateManager.popMatrix();
            GlStateManager.enableTexture();
            GlStateManager.depthMask(true);
        }
    }
}
