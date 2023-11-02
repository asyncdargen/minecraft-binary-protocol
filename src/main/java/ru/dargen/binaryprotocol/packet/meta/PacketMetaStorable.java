package ru.dargen.binaryprotocol.packet.meta;

import ru.dargen.binaryprotocol.packet.BinaryBuf;

import java.util.Optional;

public interface PacketMetaStorable {

    void storeMeta(String key, Object value);

    <T> Optional<T> getMeta(String key);

    <T> T getMetaUnsafe(String key);

    boolean hasMeta(String key);

    boolean removeMeta(String key);

    void clearMeta();

}
