/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.caffeinemc.mods.sodium.client.render.frapi.mesh;

import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.client.model.quad.BakedQuadView;
import net.caffeinemc.mods.sodium.client.render.frapi.SodiumRenderer;
import net.caffeinemc.mods.sodium.client.render.frapi.helper.ColorHelper;
import net.caffeinemc.mods.sodium.client.render.frapi.helper.TextureHelper;
import net.caffeinemc.mods.sodium.client.render.frapi.material.RenderMaterialImpl;
import dev.vexor.radium.frapi.api.renderer.v1.material.RenderMaterial;
import dev.vexor.radium.frapi.api.renderer.v1.mesh.QuadEmitter;
import dev.vexor.radium.frapi.api.renderer.v1.mesh.QuadView;
import dev.vexor.radium.frapi.api.renderer.v1.model.SpriteFinder;
import net.legacyfabric.fabric.api.util.TriState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import static net.caffeinemc.mods.sodium.client.render.frapi.mesh.EncodingFormat.*;

/**
 * Almost-concrete implementation of a mutable quad. The only missing part is {@link #emitDirectly()},
 * because that depends on where/how it is used. (Mesh encoding vs. render-time transformation).
 *
 * <p>In many cases an instance of this class is used as an "editor quad". The editor quad's
 * {@link #emitDirectly()} method calls some other internal method that transforms the quad
 * data and then buffers it. Transformations should be the same as they would be in a vanilla
 * render - the editor is serving mainly as a way to access vertex data without magical
 * numbers. It also allows for a consistent interface for those transformations.
 */
public abstract class MutableQuadViewImpl extends QuadViewImpl implements QuadEmitter {
    @Nullable
    private Sprite cachedSprite;

    /** Used for quick clearing of quad buffers. Implicitly has invalid geometry. */
    static final int[] DEFAULT = EMPTY.clone();

    static {
        MutableQuadViewImpl quad = new MutableQuadViewImpl() {
            @Override
            protected void emitDirectly() {
                // This quad won't be emitted. It's only used to configure the default quad data.
            }
        };

        // Start with all zeroes
        quad.data = DEFAULT;
        // Apply non-zero defaults
        quad.color(-1, -1, -1, -1);
        quad.cullFace(null);
        quad.material(SodiumRenderer.STANDARD_MATERIAL);
        quad.tintIndex(-1);
    }

    public void cachedSprite(@Nullable Sprite sprite) {
        cachedSprite = sprite;
    }

    public Sprite sprite(SpriteFinder finder) {
        Sprite sprite = cachedSprite;

        if (sprite == null) {
            cachedSprite = sprite = finder.find(this);
        }

        return sprite;
    }

    public void clear() {
        System.arraycopy(DEFAULT, 0, data, baseIndex, EncodingFormat.TOTAL_STRIDE);
        isGeometryInvalid = true;
        nominalFace = null;
        cachedSprite(null);
    }

    @Override
    public void load() {
        super.load();
        cachedSprite(null);
    }

    @Override
    public MutableQuadViewImpl pos(int vertexIndex, float x, float y, float z) {
        final int index = baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_X;
        data[index] = Float.floatToRawIntBits(x);
        data[index + 1] = Float.floatToRawIntBits(y);
        data[index + 2] = Float.floatToRawIntBits(z);
        isGeometryInvalid = true;
        return this;
    }

    @Override
    public MutableQuadViewImpl color(int vertexIndex, int color) {
        data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_COLOR] = color;
        return this;
    }

    @Override
    public MutableQuadViewImpl uv(int vertexIndex, float u, float v) {
        final int i = baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_U;
        data[i] = Float.floatToRawIntBits(u);
        data[i + 1] = Float.floatToRawIntBits(v);
        cachedSprite(null);
        return this;
    }

    @Override
    public MutableQuadViewImpl spriteBake(Sprite sprite, int bakeFlags) {
        TextureHelper.bakeSprite(this, sprite, bakeFlags);
        cachedSprite(sprite);
        return this;
    }

    @Override
    public MutableQuadViewImpl lightmap(int vertexIndex, int lightmap) {
        data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_LIGHTMAP] = lightmap;
        return this;
    }

    protected void normalFlags(int flags) {
        data[baseIndex + HEADER_BITS] = EncodingFormat.normalFlags(data[baseIndex + HEADER_BITS], flags);
    }

    @Override
    public MutableQuadViewImpl normal(int vertexIndex, float x, float y, float z) {
        normalFlags(normalFlags() | (1 << vertexIndex));
        data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_NORMAL] = NormI8.pack(x, y, z);
        return this;
    }

    @Override
    public final MutableQuadViewImpl cullFace(@Nullable Direction face) {
        data[baseIndex + HEADER_BITS] = EncodingFormat.cullFace(data[baseIndex + HEADER_BITS], face);
        nominalFace(face);
        return this;
    }

    @Override
    public final MutableQuadViewImpl nominalFace(@Nullable Direction face) {
        nominalFace = face;
        return this;
    }

    @Override
    public final MutableQuadViewImpl material(RenderMaterial material) {
        data[baseIndex + HEADER_BITS] = EncodingFormat.material(data[baseIndex + HEADER_BITS], (RenderMaterialImpl) material);
        return this;
    }

    @Override
    public final MutableQuadViewImpl tintIndex(int tintIndex) {
        data[baseIndex + HEADER_TINT_INDEX] = tintIndex;
        return this;
    }

    @Override
    public final MutableQuadViewImpl tag(int tag) {
        data[baseIndex + HEADER_TAG] = tag;
        return this;
    }

    /**
     * Does the same work as {@link #fromVanilla(int[], int)}, but does not mark the geometry as invalid
     * and does not clear the cached sprite.
     * Only use this if you are also setting the geometry and sprite.
     */
    private void fromVanillaInternal(int[] quadData, int startIndex) {
        System.arraycopy(quadData, startIndex, data, baseIndex + HEADER_STRIDE, QuadView.VANILLA_QUAD_STRIDE);

        int colorIndex = baseIndex + VERTEX_COLOR;

        for (int i = 0; i < 4; i++) {
            data[colorIndex] = ColorHelper.fromVanillaColor(data[colorIndex]);
            colorIndex += VERTEX_STRIDE;
        }
    }

    @Override
    public final MutableQuadViewImpl fromVanilla(int[] quadData, int startIndex) {
        fromVanillaInternal(quadData, startIndex);
        isGeometryInvalid = true;
        cachedSprite(null);
        return this;
    }

    @Override
    public final MutableQuadViewImpl fromVanilla(BakedQuad quad, RenderMaterial material, @Nullable Direction cullFace) {
        fromVanillaInternal(quad.getVertexData(), 0);
        data[baseIndex + HEADER_BITS] = EncodingFormat.cullFace(0, cullFace);
        nominalFace(quad.getFace());
        tintIndex(quad.getColorIndex());

        // TODO: Is this the same as hasShade?
        if (!((BakedQuadView) quad).hasShade()) {
            material = RenderMaterialImpl.setDisableDiffuse((RenderMaterialImpl) material, true);
        }

        if (material.ambientOcclusion().orElse(true) && !((BakedQuadView) quad).hasAO()) {
            material = RenderMaterialImpl.setAmbientOcclusion((RenderMaterialImpl) material, TriState.FALSE);
        }

        material(material);
        tag(0);

        // Copy geometry cached inside the quad
        BakedQuadView bakedView = (BakedQuadView) quad;
        NormI8.unpack(bakedView.getFaceNormal(), faceNormal);
        data[baseIndex + HEADER_FACE_NORMAL] = bakedView.getFaceNormal();
        int headerBits = EncodingFormat.lightFace(data[baseIndex + HEADER_BITS], bakedView.getLightFace());
        headerBits = EncodingFormat.normalFace(headerBits, bakedView.getNormalFace());
        data[baseIndex + HEADER_BITS] = EncodingFormat.geometryFlags(headerBits, bakedView.getFlags());
        isGeometryInvalid = false;

        return this;
    }

    /**
     * Emit the quad without clearing the underlying data.
     * Geometry is not guaranteed to be valid when called, but can be computed by calling {@link #computeGeometry()}.
     */
    protected abstract void emitDirectly();

    /**
     * Apply transforms and then if transforms return true, emit the quad without clearing the underlying data.
     */
    public final void transformAndEmit() {
        emitDirectly();
    }

    @Override
    public final MutableQuadViewImpl emit() {
        transformAndEmit();
        clear();
        return this;
    }
}
