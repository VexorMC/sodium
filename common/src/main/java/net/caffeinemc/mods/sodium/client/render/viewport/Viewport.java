package net.caffeinemc.mods.sodium.client.render.viewport;

import dev.vexor.radium.compat.mojang.math.Mth;
import dev.vexor.radium.compat.mojang.minecraft.math.SectionPos;
import net.caffeinemc.mods.sodium.client.render.viewport.frustum.Frustum;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3d;

public final class Viewport {
    private final Frustum frustum;
    private final CameraTransform transform;

    private final SectionPos sectionCoords;
    private final BlockPos blockCoords;

    public Viewport(Frustum frustum, Vector3d position) {
        this.frustum = frustum;
        this.transform = new CameraTransform(position.x, position.y, position.z);

        this.sectionCoords = SectionPos.of(
                SectionPos.posToSectionCoord(position.x),
                SectionPos.posToSectionCoord(position.y),
                SectionPos.posToSectionCoord(position.z)
        );

        this.blockCoords = new BlockPos(
                Mth.floor(position.x),
                Mth.floor(position.y),
                Mth.floor(position.z)
        );
    }

    public boolean isBoxVisible(int intOriginX, int intOriginY, int intOriginZ, float floatSizeX, float floatSizeY, float floatSizeZ) {
        return this.frustum.testAab(
                intOriginX,
                intOriginY,
                intOriginZ,

                floatSizeX,
                floatSizeY,
                floatSizeZ
        );
    }

    public CameraTransform getTransform() {
        return this.transform;
    }

    public SectionPos getChunkCoord() {
        return this.sectionCoords;
    }

    public BlockPos getBlockCoord() {
        return this.blockCoords;
    }
}
