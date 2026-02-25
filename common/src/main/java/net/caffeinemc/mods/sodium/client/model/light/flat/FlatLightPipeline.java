package net.caffeinemc.mods.sodium.client.model.light.flat;

import dev.vexor.radium.compat.mojang.minecraft.render.LightTexture;
import net.caffeinemc.mods.sodium.client.model.light.LightPipeline;
import net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFlags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Arrays;

import static net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess.*;

/**
 * A light pipeline which implements "classic-style" lighting through simply using the light value of the adjacent
 * block to a face.
 */
public class FlatLightPipeline implements LightPipeline {
    /**
     * The cache which light data will be accessed from.
     */
    private final LightDataAccess lightCache;

    public FlatLightPipeline(LightDataAccess lightCache) {
        this.lightCache = lightCache;
    }

    @Override
    public void calculate(ModelQuadView quad, BlockPos pos, QuadLightData out, Direction cullFace, Direction lightFace, boolean shade, boolean enhanced) {
        int lightmap;

        // To match vanilla behavior, use the cull face if it exists/is available
        if (cullFace != null) {
            lightmap = getOffsetLightmap(pos, cullFace);
        } else {
            lightmap = getOffsetLightmap(pos, lightFace);
        }

        Arrays.fill(out.lm, lightmap);
        Arrays.fill(out.br, this.lightCache.getLevel().getBrightness(lightFace, shade));
    }

    /**
     * When vanilla computes an offset lightmap with flat lighting, it passes the original BlockState but the
     * offset BlockPos to {@link LevelRenderer#getLightColor(BlockAndTintGetter, BlockState, BlockPos)}.
     * This does not make much sense but fixes certain issues, primarily dark quads on light-emitting blocks
     * behind tinted glass. {@link LightDataAccess} cannot efficiently store lightmaps computed with
     * inconsistent values so this method exists to mirror vanilla behavior as closely as possible.
     */
    private int getOffsetLightmap(BlockPos pos, Direction face) {
        int word = this.lightCache.get(pos);

        // Check emissivity of the origin state
        if (unpackEM(word)) {
            return LightTexture.FULL_BRIGHT;
        }

        int adjWord = this.lightCache.get(pos, face);
        
        if (unpackFO(adjWord)) {
            return LightTexture.pack(Math.max(unpackBL(word), unpackLU(word)), unpackSL(word));
        }

        return LightTexture.pack(Math.max(unpackBL(adjWord), unpackLU(word)), unpackSL(adjWord));
    }
}
