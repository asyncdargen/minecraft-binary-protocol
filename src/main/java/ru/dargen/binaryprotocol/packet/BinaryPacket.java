package ru.dargen.binaryprotocol.packet;

import lombok.NoArgsConstructor;
import ru.dargen.binaryprotocol.packet.meta.PacketMetaStorable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@NoArgsConstructor
@SuppressWarnings("unchecked")
public abstract class BinaryPacket implements PacketMetaStorable {

    protected Map<String, Object> meta;

    public BinaryPacket(BinaryBuf buf) {
        read(buf);
    }

    public void write(BinaryBuf buffer) {

    }

    public void read(BinaryBuf buffer) {

    }

    public abstract int getId();

    @Override
    public void storeMeta(String key, Object value) {
        if (meta == null) {
            meta = new HashMap<>();
        }

        meta.put(key, value);
    }

    @Override
    public <T> Optional<T> getMeta(String key) {
        if (meta == null) {
            return Optional.empty();
        }

        return Optional.ofNullable((T) meta.get(key));
    }

    @Override
    public <T> T getMetaUnsafe(String key) {
        var meta = this.<T>getMeta(key);

        return meta.orElse(null);
    }

    @Override
    public boolean hasMeta(String key) {
        return meta != null && meta.containsKey(key);
    }

    @Override
    public boolean removeMeta(String key) {
        if (meta != null) {
            return meta.remove(key) != null;
        }

        return false;
    }

    @Override
    public void clearMeta() {
        if (meta != null) {
            meta.clear();
            meta = null;
        }
    }

    public void applyMeta(BinaryBuf buffer) {
        if (meta != null) {
            meta.forEach(buffer::storeMeta);
        }
    }

    public BinaryBuf build(BinaryBuf buffer) {
        buffer.writeVarInt(getId());
        write(buffer);
        applyMeta(buffer);
        return buffer;
    }

    public static BinaryPacket createPacket(int packetId, Consumer<BinaryBuf> builder) {
        return new BinaryPacket() {
            @Override
            public void write(BinaryBuf buffer) {
                builder.accept(buffer);
            }

            @Override
            public int getId() {
                return packetId;
            }
        };
    }

}
