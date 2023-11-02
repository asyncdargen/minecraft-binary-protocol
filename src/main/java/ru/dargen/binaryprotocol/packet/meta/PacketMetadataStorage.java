package ru.dargen.binaryprotocol.packet.meta;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.buffer.ByteBuf;
import lombok.experimental.UtilityClass;
import ru.dargen.binaryprotocol.packet.BinaryBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@UtilityClass
@SuppressWarnings("unchecked")
public class PacketMetadataStorage {

    public final Cache<ByteBuf, Map<String, Object>> META_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build();

    public void storeMeta(ByteBuf packet, String key, Object value) {
        if (packet instanceof BinaryBuf binary) {
            packet = binary.asByteBuf();
        }

        var meta = META_CACHE.asMap().computeIfAbsent(packet, k -> new HashMap<>());

        meta.put(key, value);
    }

    public <T> Optional<T> getMeta(ByteBuf packet, String key) {
        if (packet instanceof BinaryBuf binary) {
            packet = binary.asByteBuf();
        }

        var meta = META_CACHE.getIfPresent(packet);
        if (meta == null) {
            return Optional.empty();
        }

        return Optional.ofNullable((T) meta.get(key));
    }

    public <T> T getMetaUnsafe(ByteBuf packet, String key) {
        if (packet instanceof BinaryBuf binary) {
            packet = binary.asByteBuf();
        }

        var meta = PacketMetadataStorage.<T>getMeta(packet, key);
        return meta.orElse(null);

    }

    public boolean removeMeta(ByteBuf packet, String key) {
        if (packet instanceof BinaryBuf binary) {
            packet = binary.asByteBuf();
        }

        var meta = META_CACHE.getIfPresent(packet);
        if (meta == null) {
            return false;
        }

        return meta.remove(key) != null;
    }

    public void clearMeta(ByteBuf packet) {
        if (packet instanceof BinaryBuf binary) {
            packet = binary.asByteBuf();
        }

        META_CACHE.invalidate(packet);
    }

}
