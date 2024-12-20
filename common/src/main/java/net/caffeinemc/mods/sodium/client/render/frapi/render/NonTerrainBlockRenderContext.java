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

package net.caffeinemc.mods.sodium.client.render.frapi.render;

import dev.lunasa.compat.mojang.blaze3d.vertex.PoseStack;
import dev.lunasa.compat.mojang.blaze3d.vertex.VertexConsumer;
import dev.lunasa.compat.mojang.minecraft.BlockColors;
import dev.lunasa.compat.mojang.minecraft.random.RandomSource;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.api.util.ColorMixer;
import net.caffeinemc.mods.sodium.client.model.light.LightMode;
import net.caffeinemc.mods.sodium.client.model.light.LightPipelineProvider;
import net.caffeinemc.mods.sodium.client.model.light.data.SingleBlockLightDataCache;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.render.texture.SpriteFinderCache;
import net.caffeinemc.mods.sodium.client.render.texture.SpriteUtil;
import net.caffeinemc.mods.sodium.client.services.SodiumModelData;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.material.ShadeMode;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.legacyfabric.fabric.api.util.TriState;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class NonTerrainBlockRenderContext extends AbstractBlockRenderContext {
    private final BlockColors colorMap;
    private final SingleBlockLightDataCache lightDataCache = new SingleBlockLightDataCache();

    private VertexConsumer vertexConsumer;
    private Matrix4f matPosition;
    private boolean trustedNormals;
    private Matrix3f matNormal;
    private int overlay;

    public NonTerrainBlockRenderContext(BlockColors colorMap) {
        this.colorMap = colorMap;
        this.lighters = new LightPipelineProvider(this.lightDataCache);
    }

    public void renderModel(BlockView blockView, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack, VertexConsumer buffer, boolean cull, RandomSource random, long seed, int overlay) {
        this.level = blockView;
        this.state = state;
        this.pos = pos;

        this.random = random;
        this.randomSeed = seed;

        this.vertexConsumer = buffer;
        this.matPosition = poseStack.last().pose();
        this.trustedNormals = poseStack.last().trustedNormals;
        this.matNormal = poseStack.last().normal();
        this.overlay = overlay;
        this.type = state.getBlock().getRenderLayerType();
        this.modelData = SodiumModelData.EMPTY;

        this.lightDataCache.reset(pos, (LevelSlice) blockView);
        this.prepareCulling(cull);
        this.prepareAoInfo(model.useAmbientOcclusion());

        ((FabricBakedModel) model).emitBlockQuads(getEmitter(), blockView, state, pos, this.randomSupplier, this::isFaceCulled);

        this.level = null;
        this.type = null;
        this.modelData = null;
        this.lightDataCache.release();
        this.random = null;
        this.vertexConsumer = null;
    }

    @Override
    protected void processQuad(MutableQuadViewImpl quad) {
        final RenderMaterial mat = quad.material();
        final TriState aoMode = mat.ambientOcclusion();
        final ShadeMode shadeMode = mat.shadeMode();
        final LightMode lightMode;
        if (aoMode == TriState.DEFAULT) {
            lightMode = this.defaultLightMode;
        } else {
            lightMode = this.useAmbientOcclusion && aoMode.get() ? LightMode.SMOOTH : LightMode.FLAT;
        }
        final boolean emissive = mat.emissive();

        tintQuad(quad);
        shadeQuad(quad, lightMode, emissive, shadeMode);
        bufferQuad(quad);
    }

    private void tintQuad(MutableQuadViewImpl quad) {
        if (quad.tintIndex() != -1) {
            final int blockColor = 0xFF000000 | this.colorMap.getColor(this.state);

            for (int i = 0; i < 4; i++) {
                quad.color(i, ColorMixer.mulComponentWise(blockColor, quad.color(i)));
            }
        }
    }

    @Override
    protected void shadeQuad(MutableQuadViewImpl quad, LightMode lightMode, boolean emissive, ShadeMode shadeMode) {
        super.shadeQuad(quad, lightMode, emissive, shadeMode);

        float[] brightnesses = this.quadLightData.br;

        for (int i = 0; i < 4; i++) {
            quad.color(i, ColorARGB.mulRGB(quad.color(i), brightnesses[i]));
        }
    }

    private void bufferQuad(MutableQuadViewImpl quad) {
        QuadEncoder.writeQuadVertices(quad, vertexConsumer, overlay, matPosition, trustedNormals, matNormal);
        SpriteUtil.markSpriteActive(quad.sprite(SpriteFinderCache.forBlockAtlas()));
    }
}
