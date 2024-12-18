package dev.lunasa.compat.mojang.minecraft.math;

import net.minecraft.util.math.*;

public class SectionPos
        extends Vec3i {
    public static final int SECTION_BITS = 4;
    public static final int SECTION_SIZE = 16;
    public static final int SECTION_MASK = 15;
    public static final int SECTION_HALF_SIZE = 8;
    public static final int SECTION_MAX_INDEX = 15;
    private static final int PACKED_X_LENGTH = 22;
    private static final int PACKED_Y_LENGTH = 20;
    private static final int PACKED_Z_LENGTH = 22;
    private static final long PACKED_X_MASK = 0x3FFFFFL;
    private static final long PACKED_Y_MASK = 1048575L;
    private static final long PACKED_Z_MASK = 0x3FFFFFL;
    private static final int Y_OFFSET = 0;
    private static final int Z_OFFSET = 20;
    private static final int X_OFFSET = 42;
    private static final int RELATIVE_X_SHIFT = 8;
    private static final int RELATIVE_Y_SHIFT = 0;
    private static final int RELATIVE_Z_SHIFT = 4;

    SectionPos(int n, int n2, int n3) {
        super(n, n2, n3);
    }

    public static SectionPos of(int n, int n2, int n3) {
        return new SectionPos(n, n2, n3);
    }

    public static SectionPos of(BlockPos blockPos) {
        return new SectionPos(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getY()), SectionPos.blockToSectionCoord(blockPos.getZ()));
    }

    public static SectionPos of(ChunkPos chunkPos, int n) {
        return new SectionPos(chunkPos.x, n, chunkPos.z);
    }

    public static SectionPos of(long l) {
        return new SectionPos(SectionPos.x(l), SectionPos.y(l), SectionPos.z(l));
    }

    public static long offset(long l, Direction direction) {
        return SectionPos.offset(l, direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ());
    }

    public static long offset(long l, int n, int n2, int n3) {
        return SectionPos.asLong(SectionPos.x(l) + n, SectionPos.y(l) + n2, SectionPos.z(l) + n3);
    }

    public static int posToSectionCoord(double d) {
        return SectionPos.blockToSectionCoord(MathHelper.floor(d));
    }

    public static int blockToSectionCoord(int n) {
        return n >> 4;
    }

    public static int blockToSectionCoord(double d) {
        return MathHelper.floor(d) >> 4;
    }

    public static int sectionRelative(int n) {
        return n & 0xF;
    }

    public static short sectionRelativePos(BlockPos blockPos) {
        int n = SectionPos.sectionRelative(blockPos.getX());
        int n2 = SectionPos.sectionRelative(blockPos.getY());
        int n3 = SectionPos.sectionRelative(blockPos.getZ());
        return (short)(n << 8 | n3 << 4 | n2 << 0);
    }

    public static int sectionRelativeX(short s) {
        return s >>> 8 & 0xF;
    }

    public static int sectionRelativeY(short s) {
        return s >>> 0 & 0xF;
    }

    public static int sectionRelativeZ(short s) {
        return s >>> 4 & 0xF;
    }

    public int relativeToBlockX(short s) {
        return this.minBlockX() + SectionPos.sectionRelativeX(s);
    }

    public int relativeToBlockY(short s) {
        return this.minBlockY() + SectionPos.sectionRelativeY(s);
    }

    public int relativeToBlockZ(short s) {
        return this.minBlockZ() + SectionPos.sectionRelativeZ(s);
    }

    public BlockPos relativeToBlockPos(short s) {
        return new BlockPos(this.relativeToBlockX(s), this.relativeToBlockY(s), this.relativeToBlockZ(s));
    }

    public static int sectionToBlockCoord(int n) {
        return n << 4;
    }

    public static int sectionToBlockCoord(int n, int n2) {
        return SectionPos.sectionToBlockCoord(n) + n2;
    }

    public static int x(long l) {
        return (int)(l << 0 >> 42);
    }

    public static int y(long l) {
        return (int)(l << 44 >> 44);
    }

    public static int z(long l) {
        return (int)(l << 22 >> 42);
    }

    public int x() {
        return this.getX();
    }

    public int y() {
        return this.getY();
    }

    public int z() {
        return this.getZ();
    }

    public int minBlockX() {
        return SectionPos.sectionToBlockCoord(this.x());
    }

    public int minBlockY() {
        return SectionPos.sectionToBlockCoord(this.y());
    }

    public int minBlockZ() {
        return SectionPos.sectionToBlockCoord(this.z());
    }

    public int maxBlockX() {
        return SectionPos.sectionToBlockCoord(this.x(), 15);
    }

    public int maxBlockY() {
        return SectionPos.sectionToBlockCoord(this.y(), 15);
    }

    public int maxBlockZ() {
        return SectionPos.sectionToBlockCoord(this.z(), 15);
    }

    public static long blockToSection(long l) {
        BlockPos pos = BlockPos.fromLong(l);

        return SectionPos.asLong(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getY()), SectionPos.blockToSectionCoord(pos.getZ()));
    }

    public static long getZeroNode(int n, int n2) {
        return SectionPos.getZeroNode(SectionPos.asLong(n, 0, n2));
    }

    public static long getZeroNode(long l) {
        return l & 0xFFFFFFFFFFF00000L;
    }

    public BlockPos origin() {
        return new BlockPos(SectionPos.sectionToBlockCoord(this.x()), SectionPos.sectionToBlockCoord(this.y()), SectionPos.sectionToBlockCoord(this.z()));
    }

    public ChunkPos chunk() {
        return new ChunkPos(this.x(), this.z());
    }

    public static long asLong(BlockPos blockPos) {
        return SectionPos.asLong(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getY()), SectionPos.blockToSectionCoord(blockPos.getZ()));
    }

    public static long asLong(int n, int n2, int n3) {
        long l = 0L;
        l |= ((long)n & 0x3FFFFFL) << 42;
        l |= ((long)n2 & 0xFFFFFL) << 0;
        return l |= ((long)n3 & 0x3FFFFFL) << 20;
    }

    public long asLong() {
        return SectionPos.asLong(this.x(), this.y(), this.z());
    }
}
