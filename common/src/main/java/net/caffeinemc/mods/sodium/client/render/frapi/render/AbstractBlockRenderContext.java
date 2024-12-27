package net.caffeinemc.mods.sodium.client.render.frapi.render;

import dev.vexor.radium.compat.mojang.minecraft.random.RandomSource;
import net.caffeinemc.mods.sodium.client.model.light.LightMode;
import net.caffeinemc.mods.sodium.client.model.light.LightPipeline;
import net.caffeinemc.mods.sodium.client.model.light.LightPipelineProvider;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache;
import net.caffeinemc.mods.sodium.client.render.frapi.SodiumRenderer;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.EncodingFormat;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.services.PlatformBlockAccess;
import net.caffeinemc.mods.sodium.client.services.PlatformModelAccess;
import net.caffeinemc.mods.sodium.client.services.SodiumModelData;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import dev.vexor.radium.frapi.api.renderer.v1.material.BlendMode;
import dev.vexor.radium.frapi.api.renderer.v1.material.RenderMaterial;
import dev.vexor.radium.frapi.api.renderer.v1.material.ShadeMode;
import dev.vexor.radium.frapi.api.renderer.v1.mesh.QuadEmitter;
import dev.vexor.radium.frapi.api.renderer.v1.model.ModelHelper;
import net.legacyfabric.fabric.api.util.TriState;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Base class for the functions that can be shared between the terrain and non-terrain pipelines.
 *
 * <p>Make sure to set the {@link #lighters} in the subclass constructor.
 */
public abstract class AbstractBlockRenderContext extends AbstractRenderContext {
    private static final RenderMaterial[] STANDARD_MATERIALS;
    private static final RenderMaterial TRANSLUCENT_MATERIAL = SodiumRenderer.INSTANCE.materialFinder().blendMode(BlendMode.TRANSLUCENT).find();

    static {
        STANDARD_MATERIALS = new RenderMaterial[AmbientOcclusionMode.values().length];

        AmbientOcclusionMode[] values = AmbientOcclusionMode.values();
        for (int i = 0; i < values.length; i++) {
            TriState state = switch (values[i]) {
                case ENABLED -> TriState.TRUE;
                case DISABLED -> TriState.FALSE;
                case DEFAULT -> TriState.DEFAULT;
            };

            STANDARD_MATERIALS[i] = SodiumRenderer.INSTANCE.materialFinder().ambientOcclusion(state).find();
        }
    }

    public class BlockEmitter extends MutableQuadViewImpl {
        {
            data = new int[EncodingFormat.TOTAL_STRIDE];
            clear();
        }

        public void bufferDefaultModel(BakedModel model, BlockState state, Predicate<Direction> cullTest) {
            AbstractBlockRenderContext.this.bufferDefaultModel(model, state, cullTest);
        }

        @Override
        public void emitDirectly() {
            if (type == null) {
                throw new IllegalStateException("No render type is set but an FRAPI object was asked to render!");
            }
            renderQuad(this);
        }
    }



    private final BlockEmitter editorQuad = new BlockEmitter();

    /**
     * The world which the block is being rendered in.
     */
    protected BlockView level;
    /**
     * The level slice used for rendering
     */
    protected LevelSlice slice;
    /**
     * The state of the block being rendered.
     */
    protected BlockState state;
    /**
     * The position (in world space) of the block being rendered.
     */
    protected BlockPos pos;

    /**
     * The current render type being rendered.
     */
    protected RenderLayer type;

    /**
     * The current model's model data.
     */
    protected SodiumModelData modelData;

    private final BlockOcclusionCache occlusionCache = new BlockOcclusionCache();
    private boolean enableCulling = true;
    // Cull cache (as it's checked per-quad instead of once per side like in vanilla)
    private int cullCompletionFlags;
    private int cullResultFlags;

    protected RandomSource random;
    protected long randomSeed;
    protected final Supplier<RandomSource> randomSupplier = () -> {
        random.setSeed(randomSeed);
        return random;
    };

    /**
     * Must be set by the subclass constructor.
     */
    protected LightPipelineProvider lighters;
    protected final QuadLightData quadLightData = new QuadLightData();
    protected boolean useAmbientOcclusion;
    // Default AO mode for model (can be overridden by material property)
    protected LightMode defaultLightMode;

    @Override
    public QuadEmitter getEmitter() {
        this.editorQuad.clear();
        return this.editorQuad;
    }

    public boolean isFaceCulled(@Nullable Direction face) {
        if (face == null || !this.enableCulling) {
            return false;
        }

        final int mask = 1 << face.getId();

        if ((this.cullCompletionFlags & mask) == 0) {
            this.cullCompletionFlags |= mask;

            if (this.occlusionCache.shouldDrawSide(this.level, this.pos, face)) {
                this.cullResultFlags |= mask;
                return false;
            } else {
                return true;
            }
        } else {
            return (this.cullResultFlags & mask) == 0;
        }
    }

    /**
     * Pipeline entrypoint - handles transform and culling checks.
     */
    private void renderQuad(MutableQuadViewImpl quad) {
        if (this.isFaceCulled(quad.cullFace())) {
            return;
        }

        this.processQuad(quad);
    }

    /**
     * Quad pipeline function - after transform and culling checks.
     * Can also be used as entrypoint to skip some logic if the transform and culling checks have already been performed.
     */
    protected abstract void processQuad(MutableQuadViewImpl quad);

    protected void prepareCulling(boolean enableCulling) {
        this.enableCulling = enableCulling;
        this.cullCompletionFlags = 0;
        this.cullResultFlags = 0;
    }

    protected void prepareAoInfo(boolean modelAo) {
        this.useAmbientOcclusion = MinecraftClient.isAmbientOcclusionEnabled();
        // Ignore the incorrect IDEA warning here.
        this.defaultLightMode = this.useAmbientOcclusion && modelAo && PlatformBlockAccess.getInstance().getLightEmission(state, level, pos) == 0 ? LightMode.SMOOTH : LightMode.FLAT;
    }

    protected void shadeQuad(MutableQuadViewImpl quad, LightMode lightMode, boolean emissive, ShadeMode shadeMode) {
        LightPipeline lighter = this.lighters.getLighter(lightMode);
        lighter.calculate(quad, this.pos, this.quadLightData, quad.cullFace(), quad.lightFace(), quad.hasShade(), shadeMode == ShadeMode.ENHANCED);
    }

    /* Handling of vanilla models - this is the hot path for non-modded models */
    public void bufferDefaultModel(BakedModel model, @Nullable BlockState state, Predicate<Direction> cullTest) {
        MutableQuadViewImpl editorQuad = this.editorQuad;


        // If there is no transform, we can check the culling face once for all the quads,
        // and we don't need to check for transforms per-quad.

        for (int i = 0; i <= ModelHelper.NULL_FACE_ID; i++) {
            final Direction cullFace = ModelHelper.faceFromIndex(i);

            if (cullTest.test(cullFace)) {
                continue;
            }

            RandomSource random = this.randomSupplier.get();
            AmbientOcclusionMode ao = PlatformBlockAccess.getInstance().usesAmbientOcclusion(model, state, modelData, type, slice, pos);

            final List<BakedQuad> quads = PlatformModelAccess.getInstance().getQuads(level, pos, model, state, cullFace, random, type, modelData);
            final int count = quads.size();

            for (final BakedQuad q : quads) {
                editorQuad.fromVanilla(q, (type == RenderLayer.TRANSLUCENT) ? TRANSLUCENT_MATERIAL : STANDARD_MATERIALS[ao.ordinal()], cullFace);
                // Call processQuad instead of emit for efficiency
                // (avoid unnecessarily clearing data, trying to apply transforms, and performing cull check again)

                editorQuad.transformAndEmit();
            }
        }

        editorQuad.clear();
    }

    public SodiumModelData getModelData() {
        return modelData;
    }

    public RenderLayer getRenderType() {
        return type;
    }
}
