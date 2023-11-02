package ru.dargen.binaryprotocol.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import ru.dargen.binaryprotocol.packet.meta.PacketMetaStorable;
import ru.dargen.binaryprotocol.packet.meta.PacketMetadataStorage;
import ru.dargen.binaryprotocol.wrapper.BlockPos;
import ru.dargen.binaryprotocol.wrapper.registry.Registries;
import ru.dargen.binaryprotocol.wrapper.registry.Registry;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class BinaryBuf extends ByteBuf implements PacketMetaStorable {

    @Delegate
    private final ByteBuf buf;

    public static BinaryBuf wrap(ByteBuf buf) {
        return new BinaryBuf(buf);
    }

    public static BinaryBuf wrap(byte[] array) {
        return wrap(Unpooled.wrappedBuffer(array));
    }

    public static BinaryBuf create() {
        return wrap(Unpooled.buffer());
    }

    public static BinaryBuf alloc(ByteBufAllocator allocator) {
        return wrap(allocator.ioBuffer());
    }

    public BinaryBuf writeVarInt(int value) {
        while (true) {
            if ((value & ~0x7F) == 0) {
                writeByte(value);
                break;
            }

            writeByte((value & 0x7F) | 0x80);

            value >>>= 7;
        }

        return this;
    }

    public int readVarInt() {
        int value = 0;
        int position = 0;
        byte currentByte;

        while (true) {
            currentByte = readByte();
            value |= (currentByte & 0x7F) << position;

            if ((currentByte & 0x80) == 0) break;

            position += 7;

            if (position >= 32) throw new RuntimeException("VarInt is too big");
        }

        return value;
    }

    public BinaryBuf writeVarLong(long value) {
        while (true) {
            if ((value & ~((long) 0x7F)) == 0) {
                writeByte((int) value);
                break;
            }

            writeByte((int) ((value & 0x7F) | 0x80));

            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value >>>= 7;
        }

        return this;
    }

    public long readVarLong() {
        long value = 0;
        int position = 0;
        byte currentByte;

        while (true) {
            currentByte = readByte();
            value |= (long) (currentByte & 0x7F) << position;

            if ((currentByte & 0x80) == 0) break;

            position += 7;

            if (position >= 64) throw new RuntimeException("VarLong is too big");
        }

        return value;
    }

    public BinaryBuf writeByteArray(byte[] array, int index, int length) {
        writeVarInt(length);
        writeBytes(array, index, length);

        return this;
    }

    public BinaryBuf writeByteArray(byte[] array, int length) {
        return writeByteArray(array, 0, length);
    }

    public BinaryBuf writeByteArray(byte[] array) {
        return writeByteArray(array, 0, array.length);
    }

    public byte[] readByteArray(byte[] array, int index, int length) {
        readBytes(array, index, length);

        return array;
    }

    public byte[] readByteArray(byte[] array) {
        return readByteArray(array, 0, array.length);
    }

    public byte[] readByteArray() {
        return readByteArray(new byte[readVarInt()]);
    }

    public BinaryBuf writeLongArray(long[] array, int index, int length) {
        writeVarInt(length);
        for (int i = index; i < length; i++) {
            writeLong(array[i]);
        }

        return this;
    }

    public BinaryBuf writeLongArray(long[] array, int length) {
        return writeLongArray(array, 0, length);
    }

    public BinaryBuf writeLongArray(long[] array) {
        return writeLongArray(array, 0, array.length);
    }

    public long[] readLongArray(long[] array, int index, int length) {
        for (int i = index; i < length; i++) {
            array[i] = readLong();
        }

        return array;
    }

    public long[] readLongArray(long[] array, int length) {
        return readLongArray(array, 0, length);
    }

    public long[] readLongArray(long[] array) {
        return readLongArray(array, 0, array.length);
    }

    public long[] readLongArray() {
        return readLongArray(new long[readVarInt()]);
    }

    public BinaryBuf writeString(String value) {
        writeByteArray(value.getBytes(StandardCharsets.UTF_8));

        return this;
    }

    public String readString() {
        return new String(readByteArray(), StandardCharsets.UTF_8);
    }

    public BinaryBuf writeKey(NamespacedKey key) {
        writeString(key.toString());

        return this;
    }

    public NamespacedKey readKey() {
        return NamespacedKey.fromString(readString());
    }

    public BinaryBuf writeUUID(UUID uuid) {
        writeLong(uuid.getMostSignificantBits());
        writeLong(uuid.getLeastSignificantBits());

        return this;
    }

    public UUID readUUID() {
        return new UUID(readLong(), readLong());
    }

    public BinaryBuf writeBlockPos(BlockPos pos) {
        writeLong(pos.asKey());

        return this;
    }

    public BlockPos readBlockPos() {
        return BlockPos.fromKey(readLong());
    }

    public <T> BinaryBuf writeRegistryValue(Registry<T> registry, T value) {
        writeVarLong(registry.indexOf(value));
        return this;
    }

    public <T> T readRegistryValue(Registry<T> registry) {
        return registry.getValue(readVarInt());
    }

    public BinaryBuf writeMaterial(Material material) {
        return writeRegistryValue(Registries.MATERIAL, material);
    }

    public Material readMaterial() {
        return readRegistryValue(Registries.MATERIAL);
    }

    public BinaryBuf writeEntityType(EntityType entityType) {
        return writeRegistryValue(Registries.ENTITY_TYPE, entityType);
    }

    public EntityType readEntityType() {
        return readRegistryValue(Registries.ENTITY_TYPE);
    }

    @Override
    public void storeMeta(String key, Object value) {
        PacketMetadataStorage.storeMeta(buf, key, value);
    }

    @Override
    public <T> Optional<T> getMeta(String key) {
        return PacketMetadataStorage.getMeta(buf, key);
    }

    @Override
    public <T> T getMetaUnsafe(String key) {
        return PacketMetadataStorage.getMetaUnsafe(buf, key);
    }

    @Override
    public boolean hasMeta(String key) {
        return getMeta(key).isPresent();
    }

    @Override
    public boolean removeMeta(String key) {
        return PacketMetadataStorage.removeMeta(buf, key);
    }

    @Override
    public void clearMeta() {
        PacketMetadataStorage.clearMeta(buf);
    }

    @Override
    public int hashCode() {
        return buf.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return buf.equals(obj);
    }

    @Override
    public String toString() {
        return "BinaryBuf[%s]".formatted(buf.toString());
    }

}
