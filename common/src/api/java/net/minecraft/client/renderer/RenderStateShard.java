package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

import net.legacyfabric.fabric.api.util.TriState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.Texture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public abstract class RenderStateShard {
    public static final double MAX_ENCHANTMENT_GLINT_SPEED_MILLIS = 8.0;
    protected final String name;
    private final Runnable setupState;
    private final Runnable clearState;
    protected static final TransparencyStateShard NO_TRANSPARENCY = new TransparencyStateShard("no_transparency", () -> GlStateManager.disableBlend(), () -> {});
    protected static final TransparencyStateShard ADDITIVE_TRANSPARENCY = new TransparencyStateShard("additive_transparency", () -> {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(BlendFactor.SourceFactor.ONE, BlendFactor.DestFactor.ONE);
    }, () -> {
        GlStateManager.disableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    });
    protected static final TransparencyStateShard LIGHTNING_TRANSPARENCY = new TransparencyStateShard("lightning_transparency", () -> {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(BlendFactor.SourceFactor.SRC_ALPHA, BlendFactor.DestFactor.ONE);
    }, () -> {
        GlStateManager.disableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    });
    protected static final TransparencyStateShard GLINT_TRANSPARENCY = new TransparencyStateShard("glint_transparency", () -> {
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(BlendFactor.SourceFactor.SRC_COLOR, BlendFactor.DestFactor.ONE, BlendFactor.SourceFactor.ZERO, BlendFactor.DestFactor.ONE);
    }, () -> {
        GlStateManager.disableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    });
    protected static final TransparencyStateShard CRUMBLING_TRANSPARENCY = new TransparencyStateShard("crumbling_transparency", () -> {
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(BlendFactor.SourceFactor.DST_COLOR, BlendFactor.DestFactor.SRC_COLOR, BlendFactor.SourceFactor.ONE, BlendFactor.DestFactor.ZERO);
    }, () -> {
        GlStateManager.disableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    });
    protected static final TransparencyStateShard OVERLAY_TRANSPARENCY = new TransparencyStateShard("overlay_transparency", () -> {
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(BlendFactor.SourceFactor.SRC_ALPHA, BlendFactor.DestFactor.ONE, BlendFactor.SourceFactor.ONE, BlendFactor.DestFactor.ZERO);
    }, () -> {
        GlStateManager.disableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    });
    protected static final TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new TransparencyStateShard("translucent_transparency", () -> {
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(BlendFactor.SourceFactor.SRC_ALPHA, BlendFactor.DestFactor.ONE_MINUS_SRC_ALPHA, BlendFactor.SourceFactor.ONE, BlendFactor.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        GlStateManager.disableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    });
    protected static final TransparencyStateShard VIGNETTE_TRANSPARENCY = new TransparencyStateShard("vignette_transparency", () -> {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(BlendFactor.SourceFactor.ZERO, BlendFactor.DestFactor.ONE_MINUS_SRC_COLOR);
    }, () -> {
        GlStateManager.disableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    });
    protected static final TransparencyStateShard CROSSHAIR_TRANSPARENCY = new TransparencyStateShard("crosshair_transparency", () -> {
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(BlendFactor.SourceFactor.ONE_MINUS_DST_COLOR, BlendFactor.DestFactor.ONE_MINUS_SRC_COLOR, BlendFactor.SourceFactor.ONE, BlendFactor.DestFactor.ZERO);
    }, () -> {
        GlStateManager.disableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    });
    protected static final TransparencyStateShard MOJANG_LOGO_TRANSPARENCY = new TransparencyStateShard("mojang_logo_transparency", () -> {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 1);
    }, () -> {
        GlStateManager.disableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    });
    protected static final TransparencyStateShard NAUSEA_OVERLAY_TRANSPARENCY = new TransparencyStateShard("nausea_overlay_transparency", () -> {
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(BlendFactor.SourceFactor.ONE, BlendFactor.DestFactor.ONE, BlendFactor.SourceFactor.ONE, BlendFactor.DestFactor.ONE);
    }, () -> {
        GlStateManager.disableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    });
    protected static final ShaderStateShard NO_SHADER = new ShaderStateShard();
    protected static final ShaderStateShard POSITION_COLOR_LIGHTMAP_SHADER = new ShaderStateShard(CoreShaders.POSITION_COLOR_LIGHTMAP);
    protected static final ShaderStateShard POSITION_SHADER = new ShaderStateShard(CoreShaders.POSITION);
    protected static final ShaderStateShard POSITION_TEX_SHADER = new ShaderStateShard(CoreShaders.POSITION_TEX);
    protected static final ShaderStateShard POSITION_COLOR_TEX_LIGHTMAP_SHADER = new ShaderStateShard(CoreShaders.POSITION_COLOR_TEX_LIGHTMAP);
    protected static final ShaderStateShard POSITION_COLOR_SHADER = new ShaderStateShard(CoreShaders.POSITION_COLOR);
    protected static final ShaderStateShard POSITION_TEXTURE_COLOR_SHADER = new ShaderStateShard(CoreShaders.POSITION_TEX_COLOR);
    protected static final ShaderStateShard PARTICLE_SHADER = new ShaderStateShard(CoreShaders.PARTICLE);
    protected static final ShaderStateShard RENDERTYPE_SOLID_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_SOLID);
    protected static final ShaderStateShard RENDERTYPE_CUTOUT_MIPPED_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_CUTOUT_MIPPED);
    protected static final ShaderStateShard RENDERTYPE_CUTOUT_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_CUTOUT);
    protected static final ShaderStateShard RENDERTYPE_TRANSLUCENT_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_TRANSLUCENT);
    protected static final ShaderStateShard RENDERTYPE_TRANSLUCENT_MOVING_BLOCK_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_TRANSLUCENT_MOVING_BLOCK);
    protected static final ShaderStateShard RENDERTYPE_ARMOR_CUTOUT_NO_CULL_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_ARMOR_CUTOUT_NO_CULL);
    protected static final ShaderStateShard RENDERTYPE_ARMOR_TRANSLUCENT_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_ARMOR_TRANSLUCENT);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_SOLID_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_ENTITY_SOLID);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_CUTOUT_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_ENTITY_CUTOUT);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_CUTOUT_NO_CULL_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_ENTITY_CUTOUT_NO_CULL);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_CUTOUT_NO_CULL_Z_OFFSET_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_ENTITY_CUTOUT_NO_CULL_Z_OFFSET);
    protected static final ShaderStateShard RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_TRANSLUCENT_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_ENTITY_TRANSLUCENT);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_SMOOTH_CUTOUT_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_ENTITY_SMOOTH_CUTOUT);
    protected static final ShaderStateShard RENDERTYPE_BEACON_BEAM_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_BEACON_BEAM);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_DECAL_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_ENTITY_DECAL);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_NO_OUTLINE_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_ENTITY_NO_OUTLINE);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_SHADOW_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_ENTITY_SHADOW);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_ALPHA_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_ENTITY_ALPHA);
    protected static final ShaderStateShard RENDERTYPE_EYES_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_EYES);
    protected static final ShaderStateShard RENDERTYPE_ENERGY_SWIRL_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_ENERGY_SWIRL);
    protected static final ShaderStateShard RENDERTYPE_LEASH_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_LEASH);
    protected static final ShaderStateShard RENDERTYPE_WATER_MASK_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_WATER_MASK);
    protected static final ShaderStateShard RENDERTYPE_OUTLINE_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_OUTLINE);
    protected static final ShaderStateShard RENDERTYPE_ARMOR_ENTITY_GLINT_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_ARMOR_ENTITY_GLINT);
    protected static final ShaderStateShard RENDERTYPE_GLINT_TRANSLUCENT_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_GLINT_TRANSLUCENT);
    protected static final ShaderStateShard RENDERTYPE_GLINT_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_GLINT);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_GLINT_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_ENTITY_GLINT);
    protected static final ShaderStateShard RENDERTYPE_CRUMBLING_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_CRUMBLING);
    protected static final ShaderStateShard RENDERTYPE_TEXT_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_TEXT);
    protected static final ShaderStateShard RENDERTYPE_TEXT_BACKGROUND_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_TEXT_BACKGROUND);
    protected static final ShaderStateShard RENDERTYPE_TEXT_INTENSITY_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_TEXT_INTENSITY);
    protected static final ShaderStateShard RENDERTYPE_TEXT_SEE_THROUGH_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_TEXT_SEE_THROUGH);
    protected static final ShaderStateShard RENDERTYPE_TEXT_BACKGROUND_SEE_THROUGH_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_TEXT_BACKGROUND_SEE_THROUGH);
    protected static final ShaderStateShard RENDERTYPE_TEXT_INTENSITY_SEE_THROUGH_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_TEXT_INTENSITY_SEE_THROUGH);
    protected static final ShaderStateShard RENDERTYPE_LIGHTNING_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_LIGHTNING);
    protected static final ShaderStateShard RENDERTYPE_TRIPWIRE_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_TRIPWIRE);
    protected static final ShaderStateShard RENDERTYPE_END_PORTAL_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_END_PORTAL);
    protected static final ShaderStateShard RENDERTYPE_END_GATEWAY_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_END_GATEWAY);
    protected static final ShaderStateShard RENDERTYPE_CLOUDS_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_CLOUDS);
    protected static final ShaderStateShard RENDERTYPE_LINES_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_LINES);
    protected static final ShaderStateShard RENDERTYPE_GUI_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_GUI);
    protected static final ShaderStateShard RENDERTYPE_GUI_OVERLAY_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_GUI_OVERLAY);
    protected static final ShaderStateShard RENDERTYPE_GUI_TEXT_HIGHLIGHT_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_GUI_TEXT_HIGHLIGHT);
    protected static final ShaderStateShard RENDERTYPE_GUI_GHOST_RECIPE_OVERLAY_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_GUI_GHOST_RECIPE_OVERLAY);
    protected static final ShaderStateShard RENDERTYPE_BREEZE_WIND_SHADER = new ShaderStateShard(CoreShaders.RENDERTYPE_BREEZE_WIND);
    protected static final TextureStateShard BLOCK_SHEET_MIPPED = new TextureStateShard(SpriteAtlasTexture.BLOCK_ATLAS_TEX, TriState.FALSE, true);
    protected static final TextureStateShard BLOCK_SHEET = new TextureStateShard(SpriteAtlasTexture.BLOCK_ATLAS_TEX, TriState.FALSE, false);
    protected static final EmptyTextureStateShard NO_TEXTURE = new EmptyTextureStateShard();
    protected static final TexturingStateShard DEFAULT_TEXTURING = new TexturingStateShard("default_texturing", () -> {}, () -> {});
    protected static final LightmapStateShard LIGHTMAP = new LightmapStateShard(true);
    protected static final LightmapStateShard NO_LIGHTMAP = new LightmapStateShard(false);
    protected static final OverlayStateShard OVERLAY = new OverlayStateShard(true);
    protected static final OverlayStateShard NO_OVERLAY = new OverlayStateShard(false);
    protected static final CullStateShard CULL = new CullStateShard(true);
    protected static final CullStateShard NO_CULL = new CullStateShard(false);
    protected static final DepthTestStateShard NO_DEPTH_TEST = new DepthTestStateShard("always", 519);
    protected static final DepthTestStateShard EQUAL_DEPTH_TEST = new DepthTestStateShard("==", 514);
    protected static final DepthTestStateShard LEQUAL_DEPTH_TEST = new DepthTestStateShard("<=", 515);
    protected static final DepthTestStateShard GREATER_DEPTH_TEST = new DepthTestStateShard(">", 516);
    protected static final WriteMaskStateShard COLOR_DEPTH_WRITE = new WriteMaskStateShard(true, true);
    protected static final WriteMaskStateShard COLOR_WRITE = new WriteMaskStateShard(true, false);
    protected static final WriteMaskStateShard DEPTH_WRITE = new WriteMaskStateShard(false, true);
    protected static final LayeringStateShard NO_LAYERING = new LayeringStateShard("no_layering", () -> {}, () -> {});
    protected static final OutputStateShard MAIN_TARGET = new OutputStateShard("main_target", () -> MinecraftClient.getInstance().getFramebuffer().bind(false), () -> {});

    public RenderStateShard(String string, Runnable runnable, Runnable runnable2) {
        this.name = string;
        this.setupState = runnable;
        this.clearState = runnable2;
    }

    public void setupRenderState() {
        this.setupState.run();
    }

    public void clearRenderState() {
        this.clearState.run();
    }

    public String toString() {
        return this.name;
    }

    private static void setupGlintTexturing(float f) {
        long l = (long)((double)System.currentTimeMillis() * 1 * 8.0);
        float f2 = (float)(l % 110000L) / 110000.0f;
        float f3 = (float)(l % 30000L) / 30000.0f;
        Matrix4f matrix4f = new Matrix4f().translation(-f2, f3, 0.0f);
        matrix4f.rotateZ(0.17453292f).scale(f);
    }

    protected static class TransparencyStateShard
            extends RenderStateShard {
        public TransparencyStateShard(String string, Runnable runnable, Runnable runnable2) {
            super(string, runnable, runnable2);
        }
    }

    protected static class ShaderStateShard
            extends RenderStateShard {
        private final Optional<ShaderProgram> shader;

        public ShaderStateShard(ShaderProgram shaderProgram) {
            super("shader", () -> {}, () -> {});
            this.shader = Optional.of(shaderProgram);
        }

        public ShaderStateShard() {
            super("shader", () -> {}, () -> {});
            this.shader = Optional.empty();
        }

        @Override
        public String toString() {
            return this.name + "[" + String.valueOf(this.shader) + "]";
        }
    }

    protected static class TextureStateShard
            extends EmptyTextureStateShard {
        private final Optional<Identifier> texture;
        private final TriState blur;
        private final boolean mipmap;

        public TextureStateShard(Identifier resourceLocation, TriState triState, boolean bl) {
            super(() -> {
                TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
                AbstractTexture abstractTexture = (AbstractTexture) textureManager.getTexture(resourceLocation);
                abstractTexture.setFilter(triState.get(), bl);
                GlStateManager.bindTexture(abstractTexture.getGlId());
                GlStateManager.activeTexture(GL13.GL_TEXTURE0);
            }, () -> {});
            this.texture = Optional.of(resourceLocation);
            this.blur = triState;
            this.mipmap = bl;
        }

        @Override
        public String toString() {
            return this.name + "[" + String.valueOf(this.texture) + "(blur=" + String.valueOf((Object)this.blur) + ", mipmap=" + this.mipmap + ")]";
        }

        @Override
        protected Optional<Identifier> cutoutTexture() {
            return this.texture;
        }
    }

    protected static class EmptyTextureStateShard
            extends RenderStateShard {
        public EmptyTextureStateShard(Runnable runnable, Runnable runnable2) {
            super("texture", runnable, runnable2);
        }

        EmptyTextureStateShard() {
            super("texture", () -> {}, () -> {});
        }

        protected Optional<Identifier> cutoutTexture() {
            return Optional.empty();
        }
    }

    protected static class TexturingStateShard
            extends RenderStateShard {
        public TexturingStateShard(String string, Runnable runnable, Runnable runnable2) {
            super(string, runnable, runnable2);
        }
    }

    protected static class LightmapStateShard
            extends BooleanStateShard {
        public LightmapStateShard(boolean bl) {
            super("lightmap", () -> {
                if (bl) {
                    MinecraftClient.getInstance().gameRenderer.enableLightmap();
                }
            }, () -> {
                if (bl) {
                    MinecraftClient.getInstance().gameRenderer.disableLightmap();
                }
            }, bl);
        }
    }

    protected static class OverlayStateShard
            extends BooleanStateShard {
        public OverlayStateShard(boolean bl) {
            super("overlay", () -> {
                if (bl) {
                }
            }, () -> {
                if (bl) {
                }
            }, bl);
        }
    }

    protected static class CullStateShard
            extends BooleanStateShard {
        public CullStateShard(boolean bl) {
            super("cull", () -> {
                if (!bl) {
                    GlStateManager.disableCull();
                }
            }, () -> {
                if (!bl) {
                    GlStateManager.enableCull();
                }
            }, bl);
        }
    }

    protected static class DepthTestStateShard
            extends RenderStateShard {
        private final String functionName;

        public DepthTestStateShard(String string, int n) {
            super("depth_test", () -> {
                if (n != 519) {
                    GlStateManager.enableDepthTest();
                    GlStateManager.depthFunc(n);
                }
            }, () -> {
                if (n != 519) {
                    GlStateManager.disableDepthTest();
                    GlStateManager.depthFunc(515);
                }
            });
            this.functionName = string;
        }

        @Override
        public String toString() {
            return this.name + "[" + this.functionName + "]";
        }
    }

    protected static class WriteMaskStateShard
            extends RenderStateShard {
        private final boolean writeColor;
        private final boolean writeDepth;

        public WriteMaskStateShard(boolean bl, boolean bl2) {
            super("write_mask_state", () -> {
                if (!bl2) {
                    GlStateManager.depthMask(bl2);
                }
                if (!bl) {
                    GlStateManager.colorMask(bl, bl, bl, bl);
                }
            }, () -> {
                if (!bl2) {
                    GlStateManager.depthMask(true);
                }
                if (!bl) {
                    GlStateManager.colorMask(true, true, true, true);
                }
            });
            this.writeColor = bl;
            this.writeDepth = bl2;
        }

        @Override
        public String toString() {
            return this.name + "[writeColor=" + this.writeColor + ", writeDepth=" + this.writeDepth + "]";
        }
    }

    protected static class LayeringStateShard
            extends RenderStateShard {
        public LayeringStateShard(String string, Runnable runnable, Runnable runnable2) {
            super(string, runnable, runnable2);
        }
    }

    protected static class OutputStateShard
            extends RenderStateShard {
        public OutputStateShard(String string, Runnable runnable, Runnable runnable2) {
            super(string, runnable, runnable2);
        }
    }

    protected static class LineStateShard
            extends RenderStateShard {
        private final OptionalDouble width;

        public LineStateShard(OptionalDouble optionalDouble) {
            super("line_width", () -> {
                if (!Objects.equals(optionalDouble, OptionalDouble.of(1.0))) {
                    if (optionalDouble.isPresent()) {
                        GL11.glLineWidth((float)optionalDouble.getAsDouble());
                    } else {
                        GL11.glLineWidth(Math.max(2.5f, (float)MinecraftClient.getInstance().width
                                / 1920.0f * 2.5f));
                    }
                }
            }, () -> {
                if (!Objects.equals(optionalDouble, OptionalDouble.of(1.0))) {
                    GL11.glLineWidth(1.0f);
                }
            });
            this.width = optionalDouble;
        }

        @Override
        public String toString() {
            return this.name + "[" + String.valueOf(this.width.isPresent() ? Double.valueOf(this.width.getAsDouble()) : "window_scale") + "]";
        }
    }

    protected static class ColorLogicStateShard
            extends RenderStateShard {
        public ColorLogicStateShard(String string, Runnable runnable, Runnable runnable2) {
            super(string, runnable, runnable2);
        }
    }

    static class BooleanStateShard
            extends RenderStateShard {
        private final boolean enabled;

        public BooleanStateShard(String string, Runnable runnable, Runnable runnable2, boolean bl) {
            super(string, runnable, runnable2);
            this.enabled = bl;
        }

        @Override
        public String toString() {
            return this.name + "[" + this.enabled + "]";
        }
    }

    protected static final class OffsetTexturingStateShard
            extends TexturingStateShard {
        public OffsetTexturingStateShard(float f, float f2) {
            super("offset_texturing", () -> {
            }, () -> {});
        }
    }

    protected static class MultiTextureStateShard
            extends EmptyTextureStateShard {
        private final Optional<Identifier> cutoutTexture;

        MultiTextureStateShard(List<Entry> list) {
            super(() -> {
                for (int i = 0; i < list.size(); ++i) {
                    Entry entry = (Entry)list.get(i);
                    TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
                    AbstractTexture abstractTexture = (AbstractTexture) textureManager.getTexture(entry.id);
                    abstractTexture.setFilter(entry.blur, entry.mipmap);
                    GlStateManager.activeTexture(GL13.GL_TEXTURE0 + i);
                    GlStateManager.bindTexture(abstractTexture.getGlId());
                }
            }, () -> {});
            this.cutoutTexture = list.isEmpty() ? Optional.empty() : Optional.of(list.stream().findFirst().get().id);
        }

        @Override
        protected Optional<Identifier> cutoutTexture() {
            return this.cutoutTexture;
        }

        public static Builder builder() {
            return new Builder();
        }

        static final class Entry {
            final Identifier id;
            final boolean blur;
            final boolean mipmap;

            Entry(Identifier resourceLocation, boolean bl, boolean bl2) {
                this.id = resourceLocation;
                this.blur = bl;
                this.mipmap = bl2;
            }

            public Identifier id() {
                return this.id;
            }

            public boolean blur() {
                return this.blur;
            }

            public boolean mipmap() {
                return this.mipmap;
            }
        }

        public static final class Builder {
            private final ImmutableList.Builder<Entry> builder = new ImmutableList.Builder();

            public Builder add(Identifier resourceLocation, boolean bl, boolean bl2) {
                this.builder.add(new Entry(resourceLocation, bl, bl2));
                return this;
            }

            public MultiTextureStateShard build() {
                return new MultiTextureStateShard((List<Entry>)this.builder.build());
            }
        }
    }
}

