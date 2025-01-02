package net.irisshaders.iris.platform;

import net.minecraft.block.BlockState;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import java.nio.file.Path;
import java.util.ServiceLoader;

public interface IrisPlatformHelpers {
	IrisPlatformHelpers INSTANCE = ServiceLoader.load(IrisPlatformHelpers.class).findFirst().get();

	static IrisPlatformHelpers getInstance() {
		return INSTANCE;
	}

	boolean isModLoaded(String modId);

	String getVersion();

	boolean isDevelopmentEnvironment();

	Path getGameDir();

	Path getConfigDir();

	int compareVersions(String currentVersion, String semanticVersion) throws Exception;

    KeyBinding registerKeyBinding(KeyBinding keyMapping);

	boolean useELS();

    BlockState getBlockAppearance(BlockView level, BlockState state, Direction cullFace, BlockPos pos);
}
