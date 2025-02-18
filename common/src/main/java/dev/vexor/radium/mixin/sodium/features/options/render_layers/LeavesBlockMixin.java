package dev.vexor.radium.mixin.sodium.features.options.render_layers;

import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin extends Block {

    public LeavesBlockMixin(Material material, MaterialColor materialColor) {
        super(material, materialColor);
    }

    @ModifyVariable(method = "setGraphics", at = @At("HEAD"), argsOnly = true, index = 1)
    private boolean getSodiumLeavesQuality(boolean fancy) {
        return SodiumClientMod.options().quality.leavesQuality.isFancy(fancy);
    }
}
