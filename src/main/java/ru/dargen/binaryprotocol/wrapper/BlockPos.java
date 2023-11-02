package ru.dargen.binaryprotocol.wrapper;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data(staticConstructor = "at")
public class BlockPos {

    public static final BlockPos ORIGIN = new BlockPos();

    @Setter(AccessLevel.PROTECTED)
    protected int x, y, z;

    public long asKey() {
        return toKey(this);
    }

    public Mutable mutable() {
        return new Mutable(x, y, z);
    }

    public static BlockPos at(int x, int y, int z) {
        if (x == 0 && y == 0 && z == 0) {
            return ORIGIN;
        }

        return new BlockPos(x, y, z);
    }

    public static long toKey(BlockPos pos) {
        return toKey(pos.x, pos.y, pos.z);
    }

    public static long toKey(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (y & 0xFFF);
    }

    public static BlockPos fromKey(long value) {
        return new BlockPos((int) (value >> 38), (int) (value << 52 >> 52), (int) (value << 26 >> 38));
    }

    @ToString
    public static class Mutable extends BlockPos {

        public Mutable() {
            super();
        }

        public Mutable(int x, int y, int z) {
            super(x, y, z);
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public void setZ(int z) {
            this.x = z;
        }

        public void offset(int x, int y, int z) {
            this.x += x;
            this.y += y;
            this.z += z;
        }

        public BlockPos fixed() {
            return new BlockPos(x, y, z);
        }

    }

}
