package net.coderbot.iris.block_rendering;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.coderbot.iris.Iris;
import net.coderbot.iris.shaderpack.materialmap.BlockEntry;
import net.coderbot.iris.shaderpack.materialmap.BlockRenderType;
import net.coderbot.iris.shaderpack.materialmap.NamespacedId;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockMaterialMapping {
	public static Object2IntMap<BlockState> createBlockStateIdMap(Int2ObjectMap<List<BlockEntry>> blockPropertiesMap) {
		Object2IntMap<BlockState> blockStateIds = new Object2IntOpenHashMap<>();

		blockPropertiesMap.forEach((intId, entries) -> {
			for (BlockEntry entry : entries) {
				addBlockStates(entry, blockStateIds, intId);
			}
		});

		return blockStateIds;
	}

	public static Map<Block, RenderLayer> createBlockTypeMap(Map<NamespacedId, BlockRenderType> blockPropertiesMap) {
		Map<Block, RenderLayer> blockTypeIds = new Reference2ReferenceOpenHashMap<>();

		blockPropertiesMap.forEach((id, blockType) -> {
			Block block = Block.get(id.toString());

			blockTypeIds.put(block, convertBlockToRenderType(blockType));
		});

		return blockTypeIds;
	}

	private static RenderLayer convertBlockToRenderType(BlockRenderType type) {
		if (type == null) {
			return null;
		}

		switch (type) {
			case SOLID: return RenderLayer.SOLID;
			case CUTOUT: return RenderLayer.CUTOUT;
			case CUTOUT_MIPPED: return RenderLayer.CUTOUT_MIPPED;
			case TRANSLUCENT: return RenderLayer.TRANSLUCENT;
			default: return null;
		}
	}

	private static void addBlockStates(BlockEntry entry, Object2IntMap<BlockState> idMap, int intId) {

	}
}
