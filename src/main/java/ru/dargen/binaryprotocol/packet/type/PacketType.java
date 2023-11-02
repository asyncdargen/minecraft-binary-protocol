package ru.dargen.binaryprotocol.packet.type;

import ru.dargen.binaryprotocol.network.NetworkBound;
import ru.dargen.binaryprotocol.network.NetworkPhase;

public record PacketType(int id, NetworkBound bound, NetworkPhase phase) {

    public int asKey() {
        return toKey(this);
    }

    public static int toKey(int id, NetworkBound bound, NetworkPhase phase) {
        return (id & 0xFFFF) << 16 | bound.ordinal() << 8 | phase.ordinal();
    }

    public static int toKey(PacketType type) {
        return toKey(type.id, type.bound, type.phase);
    }

    public static PacketType fromKey(int key) {
        return new PacketType(key >>> 16, NetworkBound.values()[(key >>> 8) & 0xFF], NetworkPhase.values()[key & 0xFF]);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = result * 59 + id;
        result = result * 59 + bound.ordinal();
        result = result * 59 + phase.ordinal();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof PacketType other) {
            return id == other.id && bound == other.bound && phase == other.phase;
        }

        return false;
    }

    @Override
    public String toString() {
        return "PacketType[0x%s, TO:%s, %s]".formatted(Integer.toHexString(id).toUpperCase(), bound, phase);
    }

}
