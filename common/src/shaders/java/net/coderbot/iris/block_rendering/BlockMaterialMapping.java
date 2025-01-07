package net.coderbot.iris.block_rendering;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.coderbot.iris.shaderpack.materialmap.BlockEntry;
import net.coderbot.iris.shaderpack.materialmap.BlockRenderType;
import net.coderbot.iris.shaderpack.materialmap.NamespacedId;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public class BlockMaterialMapping {
    public static Object2IntMap<BlockState> createBlockStateIdMap(Int2ObjectMap<List<BlockEntry>> blockPropertiesMap) {
        Object2IntMap<BlockState> blockMatches = new Object2IntOpenHashMap<>();

        blockPropertiesMap.forEach((intId, entries) -> {
            for (BlockEntry entry : entries) {
                addBlock(entry, blockMatches, intId);
            }
        });

        return blockMatches;
    }

    public static Map<Block, RenderLayer> createBlockTypeMap(Map<NamespacedId, BlockRenderType> blockPropertiesMap) {
        Map<Block, RenderLayer> blockTypeIds = new Reference2ReferenceOpenHashMap<>();

        blockPropertiesMap.forEach((id, blockType) -> {
            Identifier resourceLocation = new Identifier(id.getNamespace(), id.getName());

            Block block = Block.get(resourceLocation.toString());

            blockTypeIds.put(block, convertBlockToRenderType(blockType));
        });

        return blockTypeIds;
    }

    private static RenderLayer convertBlockToRenderType(BlockRenderType type) {
        if (type == null) {
            return null;
        }

        return switch (type) {
            // Everything renders in cutout or translucent in 1.7.10
            case SOLID -> RenderLayer.SOLID;
            case CUTOUT -> RenderLayer.CUTOUT;
            case CUTOUT_MIPPED -> RenderLayer.CUTOUT_MIPPED;
            case TRANSLUCENT -> RenderLayer.TRANSLUCENT;
            default -> null;
        };
    }

    private static void addBlock(BlockEntry entry, Object2IntMap<BlockState> idMap, int intId) {
        final NamespacedId id = entry.getId();
        final Identifier resourceLocation = new Identifier(id.getNamespace(), id.getName());

        final Block block = (Block) Block.get(resourceLocation.toString());

        // If the block doesn't exist, by default the registry will return AIR. That probably isn't what we want.
        // TODO: Assuming that Registry.BLOCK.getDefaultId() == "minecraft:air" here
        if (block == null || block == Blocks.AIR) {
            return;
        }

        idMap.put(block.getDefaultState(), intId);
    }
}