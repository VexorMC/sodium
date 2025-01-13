package dev.vexor.radium.culling;

import com.logisticscraft.occlusionculling.DataProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public class RadiumCullingDataProvider implements DataProvider {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private ClientWorld world = null;
    
    @Override
    public boolean prepareChunk(int chunkX, int chunkZ) {
        world = client.world;
        return world != null;
    }

    @Override
    public boolean isOpaqueFullCube(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        return world.getBlockState(pos).getBlock().hasTransparency();
    }

    @Override
    public void cleanup() {
        world = null;
    }
}
