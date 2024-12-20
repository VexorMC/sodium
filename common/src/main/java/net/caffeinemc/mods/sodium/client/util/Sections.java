package net.caffeinemc.mods.sodium.client.util;

import dev.vexor.radium.compat.mojang.minecraft.math.SectionPos;
import net.minecraft.world.World;

public class Sections {

    public static int min(World world) {
        return SectionPos.blockToSectionCoord(0);
    }

    public static int max(World world) {
        return SectionPos.blockToSectionCoord(world.getEffectiveHeight());
    }

}