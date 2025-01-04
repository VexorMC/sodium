package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import dev.vexor.radium.compat.mojang.Util;
import net.legacyfabric.fabric.api.util.TriState;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class RenderType
        extends RenderStateShard {
    private static final int MEGABYTE = 0x100000;
    public static final int BIG_BUFFER_SIZE = 0x400000;
    public static final int SMALL_BUFFER_SIZE = 786432;
    public static final int TRANSIENT_BUFFER_SIZE = 1536;
    private static final RenderType SOLID = RenderType.create("solid", VertexFormats.BLOCK, VertexFormatMode.QUADS, 0x400000, true, false, CompositeState.builder().setLightmapState(LIGHTMAP).setShaderState(RENDERTYPE_SOLID_SHADER).setTextureState(BLOCK_SHEET_MIPPED).createCompositeState(true));
    private static final RenderType CUTOUT_MIPPED = RenderType.create("cutout_mipped", VertexFormats.BLOCK, VertexFormatMode.QUADS, 0x400000, true, false, CompositeState.builder().setLightmapState(LIGHTMAP).setShaderState(RENDERTYPE_CUTOUT_MIPPED_SHADER).setTextureState(BLOCK_SHEET_MIPPED).createCompositeState(true));
    private static final RenderType CUTOUT = RenderType.create("cutout", VertexFormats.BLOCK, VertexFormatMode.QUADS, 786432, true, false, CompositeState.builder().setLightmapState(LIGHTMAP).setShaderState(RENDERTYPE_CUTOUT_SHADER).setTextureState(BLOCK_SHEET).createCompositeState(true));
    private static final RenderType TRANSLUCENT = RenderType.create("translucent", VertexFormats.BLOCK, VertexFormatMode.QUADS, 786432, true, true, RenderType.translucentState(RENDERTYPE_TRANSLUCENT_SHADER));
    private static final RenderType TRIPWIRE = RenderType.create("tripwire", VertexFormats.BLOCK, VertexFormatMode.QUADS, 1536, true, true, RenderType.tripwireState());
    private final VertexFormat format;
    private final VertexFormatMode mode;
    private final int bufferSize;
    private final boolean affectsCrumbling;
    private final boolean sortOnUpload;

    public static RenderType solid() {
        return SOLID;
    }

    public static RenderType cutoutMipped() {
        return CUTOUT_MIPPED;
    }

    public static RenderType cutout() {
        return CUTOUT;
    }

    private static CompositeState translucentState(RenderStateShard.ShaderStateShard shaderStateShard) {
        return CompositeState.builder().setLightmapState(LIGHTMAP).setShaderState(shaderStateShard).setTextureState(BLOCK_SHEET_MIPPED).setTransparencyState(TRANSLUCENT_TRANSPARENCY).createCompositeState(true);
    }

    public static RenderType translucent() {
        return TRANSLUCENT;
    }

    private static CompositeState tripwireState() {
        return CompositeState.builder().setLightmapState(LIGHTMAP).setShaderState(RENDERTYPE_TRIPWIRE_SHADER).setTextureState(BLOCK_SHEET_MIPPED).setTransparencyState(TRANSLUCENT_TRANSPARENCY).createCompositeState(true);
    }

    public static RenderType tripwire() {
        return TRIPWIRE;
    }

    public RenderType(String string, VertexFormat vertexFormat, VertexFormatMode mode, int n, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
        super(string, runnable, runnable2);
        this.format = vertexFormat;
        this.mode = mode;
        this.bufferSize = n;
        this.affectsCrumbling = bl;
        this.sortOnUpload = bl2;
    }

    static CompositeRenderType create(String string, VertexFormat vertexFormat, VertexFormatMode mode, int n, CompositeState compositeState) {
        return RenderType.create(string, vertexFormat, mode, n, false, false, compositeState);
    }

    public static CompositeRenderType create(String string, VertexFormat vertexFormat, VertexFormatMode mode, int n, boolean bl, boolean bl2, CompositeState compositeState) {
        return new CompositeRenderType(string, vertexFormat, mode, n, bl, bl2, compositeState);
    }

    @Override
    public String toString() {
        return this.name;
    }

    public int bufferSize() {
        return this.bufferSize;
    }

    public VertexFormat format() {
        return this.format;
    }

    public VertexFormatMode mode() {
        return this.mode;
    }

    public Optional<RenderType> outline() {
        return Optional.empty();
    }

    public boolean isOutline() {
        return false;
    }

    public boolean affectsCrumbling() {
        return this.affectsCrumbling;
    }

    public boolean canConsolidateConsecutiveGeometry() {
        return !this.mode.connectedPrimitives;
    }

    public boolean sortOnUpload() {
        return this.sortOnUpload;
    }

    protected static final class CompositeState {
        final RenderStateShard.EmptyTextureStateShard textureState;
        private final RenderStateShard.ShaderStateShard shaderState;
        private final RenderStateShard.TransparencyStateShard transparencyState;
        private final RenderStateShard.DepthTestStateShard depthTestState;
        final RenderStateShard.CullStateShard cullState;
        private final RenderStateShard.LightmapStateShard lightmapState;
        private final RenderStateShard.OverlayStateShard overlayState;
        private final RenderStateShard.LayeringStateShard layeringState;
        private final RenderStateShard.OutputStateShard outputState;
        private final RenderStateShard.TexturingStateShard texturingState;
        private final RenderStateShard.WriteMaskStateShard writeMaskState;
        final OutlineProperty outlineProperty;
        final ImmutableList<RenderStateShard> states;

        CompositeState(RenderStateShard.EmptyTextureStateShard emptyTextureStateShard, RenderStateShard.ShaderStateShard shaderStateShard, RenderStateShard.TransparencyStateShard transparencyStateShard, RenderStateShard.DepthTestStateShard depthTestStateShard, RenderStateShard.CullStateShard cullStateShard, RenderStateShard.LightmapStateShard lightmapStateShard, RenderStateShard.OverlayStateShard overlayStateShard, RenderStateShard.LayeringStateShard layeringStateShard, RenderStateShard.OutputStateShard outputStateShard, RenderStateShard.TexturingStateShard texturingStateShard, RenderStateShard.WriteMaskStateShard writeMaskStateShard, OutlineProperty outlineProperty) {
            this.textureState = emptyTextureStateShard;
            this.shaderState = shaderStateShard;
            this.transparencyState = transparencyStateShard;
            this.depthTestState = depthTestStateShard;
            this.cullState = cullStateShard;
            this.lightmapState = lightmapStateShard;
            this.overlayState = overlayStateShard;
            this.layeringState = layeringStateShard;
            this.outputState = outputStateShard;
            this.texturingState = texturingStateShard;
            this.writeMaskState = writeMaskStateShard;
            this.outlineProperty = outlineProperty;
            this.states = ImmutableList.of(this.textureState, this.shaderState, this.transparencyState, this.depthTestState, this.cullState, this.lightmapState, this.overlayState, this.layeringState, this.outputState, this.texturingState, this.writeMaskState);
        }

        public String toString() {
            return "CompositeState[" + String.valueOf(this.states) + ", outlineProperty=" + String.valueOf(this.outlineProperty) + "]";
        }

        public static CompositeStateBuilder builder() {
            return new CompositeStateBuilder();
        }

        public static class CompositeStateBuilder {
            private RenderStateShard.EmptyTextureStateShard textureState = RenderStateShard.NO_TEXTURE;
            private RenderStateShard.ShaderStateShard shaderState = RenderStateShard.NO_SHADER;
            private RenderStateShard.TransparencyStateShard transparencyState = RenderStateShard.NO_TRANSPARENCY;
            private RenderStateShard.DepthTestStateShard depthTestState = RenderStateShard.LEQUAL_DEPTH_TEST;
            private RenderStateShard.CullStateShard cullState = RenderStateShard.CULL;
            private RenderStateShard.LightmapStateShard lightmapState = RenderStateShard.NO_LIGHTMAP;
            private RenderStateShard.OverlayStateShard overlayState = RenderStateShard.NO_OVERLAY;
            private RenderStateShard.LayeringStateShard layeringState = RenderStateShard.NO_LAYERING;
            private RenderStateShard.OutputStateShard outputState = RenderStateShard.MAIN_TARGET;
            private RenderStateShard.TexturingStateShard texturingState = RenderStateShard.DEFAULT_TEXTURING;
            private RenderStateShard.WriteMaskStateShard writeMaskState = RenderStateShard.COLOR_DEPTH_WRITE;

            CompositeStateBuilder() {
            }

            public CompositeStateBuilder setTextureState(RenderStateShard.EmptyTextureStateShard emptyTextureStateShard) {
                this.textureState = emptyTextureStateShard;
                return this;
            }

            public CompositeStateBuilder setShaderState(RenderStateShard.ShaderStateShard shaderStateShard) {
                this.shaderState = shaderStateShard;
                return this;
            }

            public CompositeStateBuilder setTransparencyState(RenderStateShard.TransparencyStateShard transparencyStateShard) {
                this.transparencyState = transparencyStateShard;
                return this;
            }

            public CompositeStateBuilder setDepthTestState(RenderStateShard.DepthTestStateShard depthTestStateShard) {
                this.depthTestState = depthTestStateShard;
                return this;
            }

            public CompositeStateBuilder setCullState(RenderStateShard.CullStateShard cullStateShard) {
                this.cullState = cullStateShard;
                return this;
            }

            public CompositeStateBuilder setLightmapState(RenderStateShard.LightmapStateShard lightmapStateShard) {
                this.lightmapState = lightmapStateShard;
                return this;
            }

            public CompositeStateBuilder setOverlayState(RenderStateShard.OverlayStateShard overlayStateShard) {
                this.overlayState = overlayStateShard;
                return this;
            }

            public CompositeStateBuilder setLayeringState(RenderStateShard.LayeringStateShard layeringStateShard) {
                this.layeringState = layeringStateShard;
                return this;
            }

            public CompositeStateBuilder setOutputState(RenderStateShard.OutputStateShard outputStateShard) {
                this.outputState = outputStateShard;
                return this;
            }

            public CompositeStateBuilder setTexturingState(RenderStateShard.TexturingStateShard texturingStateShard) {
                this.texturingState = texturingStateShard;
                return this;
            }

            public CompositeStateBuilder setWriteMaskState(RenderStateShard.WriteMaskStateShard writeMaskStateShard) {
                this.writeMaskState = writeMaskStateShard;
                return this;
            }

            public CompositeState createCompositeState(boolean bl) {
                return this.createCompositeState(bl ? OutlineProperty.AFFECTS_OUTLINE : OutlineProperty.NONE);
            }

            public CompositeState createCompositeState(OutlineProperty outlineProperty) {
                return new CompositeState(this.textureState, this.shaderState, this.transparencyState, this.depthTestState, this.cullState, this.lightmapState, this.overlayState, this.layeringState, this.outputState, this.texturingState, this.writeMaskState, outlineProperty);
            }
        }
    }

    static final class CompositeRenderType
            extends RenderType {
        static final BiFunction<Identifier, RenderStateShard.CullStateShard, RenderType> OUTLINE = Util.memoize((resourceLocation, cullStateShard) -> RenderType.create("outline", VertexFormats.POSITION_TEXTURE_COLOR, VertexFormatMode.QUADS, 1536, CompositeState.builder().setShaderState(RENDERTYPE_OUTLINE_SHADER).setTextureState(new RenderStateShard.TextureStateShard((Identifier)resourceLocation, TriState.FALSE, false)).setCullState(cullStateShard).setDepthTestState(NO_DEPTH_TEST).createCompositeState(OutlineProperty.IS_OUTLINE)));
        private final CompositeState state;
        private final Optional<RenderType> outline;
        private final boolean isOutline;

        CompositeRenderType(String string, VertexFormat vertexFormat, VertexFormatMode mode, int n, boolean bl, boolean bl2, CompositeState compositeState) {
            super(string, vertexFormat, mode, n, bl, bl2, () -> compositeState.states.forEach(RenderStateShard::setupRenderState), () -> compositeState.states.forEach(RenderStateShard::clearRenderState));
            this.state = compositeState;
            this.outline = compositeState.outlineProperty == OutlineProperty.AFFECTS_OUTLINE ? compositeState.textureState.cutoutTexture().map(resourceLocation -> OUTLINE.apply((Identifier)resourceLocation, compositeState.cullState)) : Optional.empty();
            this.isOutline = compositeState.outlineProperty == OutlineProperty.IS_OUTLINE;
        }

        @Override
        public Optional<RenderType> outline() {
            return this.outline;
        }

        @Override
        public boolean isOutline() {
            return this.isOutline;
        }

        protected final CompositeState state() {
            return this.state;
        }

        @Override
        public String toString() {
            return "RenderType[" + this.name + ":" + String.valueOf(this.state) + "]";
        }
    }

    static enum OutlineProperty {
        NONE("none"),
        IS_OUTLINE("is_outline"),
        AFFECTS_OUTLINE("affects_outline");

        private final String name;

        private OutlineProperty(String string2) {
            this.name = string2;
        }

        public String toString() {
            return this.name;
        }
    }
}

