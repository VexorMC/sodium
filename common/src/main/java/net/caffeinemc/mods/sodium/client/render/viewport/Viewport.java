package net.caffeinemc.mods.sodium.client.render.viewport;

import dev.vexor.radium.compat.mojang.math.Mth;
import dev.vexor.radium.compat.mojang.minecraft.math.SectionPos;
import net.caffeinemc.mods.sodium.client.render.viewport.frustum.Frustum;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.FrustumIntersection;
import org.joml.Vector3d;

public final class Viewport {
    private final Frustum frustum;
    private final CameraTransform transform;

    private final SectionPos sectionCoords;
    private final BlockPos blockCoords;

    public Viewport(Frustum frustum, Vec3d position) {
        this.frustum = frustum;
        this.transform = new CameraTransform(position.x, position.y, position.z);

        this.sectionCoords = SectionPos.of(
                SectionPos.posToSectionCoord(position.x),
                SectionPos.posToSectionCoord(position.y),
                SectionPos.posToSectionCoord(position.z)
        );

        this.blockCoords = new BlockPos(
                MathHelper.floor(position.x),
                MathHelper.floor(position.y),
                MathHelper.floor(position.z)
        );
    }

    public boolean isBoxVisibleDirect(float floatOriginX, float floatOriginY, float floatOriginZ, float floatSize) {
        return this.frustum.testAab(
                floatOriginX - floatSize,
                floatOriginY - floatSize,
                floatOriginZ - floatSize,

                floatOriginX + floatSize,
                floatOriginY + floatSize,
                floatOriginZ + floatSize
        );
    }

    public int getBoxIntersectionDirect(float floatOriginX, float floatOriginY, float floatOriginZ, float floatSize) {
        return this.frustum.testAab(
                floatOriginX - floatSize,
                floatOriginY - floatSize,
                floatOriginZ - floatSize,

                floatOriginX + floatSize,
                floatOriginY + floatSize,
                floatOriginZ + floatSize
        ) ? FrustumIntersection.INSIDE : FrustumIntersection.OUTSIDE;
    }

    public boolean isBoxVisible(int intOriginX, int intOriginY, int intOriginZ, float floatSizeX, float floatSizeY, float floatSizeZ) {
        float floatOriginX = intOriginX - this.transform.fracX;
        float floatOriginY = intOriginY - this.transform.fracY;
        float floatOriginZ = intOriginZ - this.transform.fracZ;

        return this.frustum.testAab(
                floatOriginX - floatSizeX,
                floatOriginY - floatSizeY,
                floatOriginZ - floatSizeZ,

                floatOriginX + floatSizeX,
                floatOriginY + floatSizeY,
                floatOriginZ + floatSizeZ
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
