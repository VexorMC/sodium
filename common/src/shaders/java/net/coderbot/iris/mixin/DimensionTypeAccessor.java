package net.coderbot.iris.mixin;

import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.OptionalLong;

@Mixin(Dimension.class)
public interface DimensionTypeAccessor {

	@Accessor
	OptionalLong getFixedTime();

	@Accessor
	float getAmbientLight();

}
