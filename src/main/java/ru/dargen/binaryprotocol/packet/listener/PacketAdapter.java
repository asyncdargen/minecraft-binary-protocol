package ru.dargen.binaryprotocol.packet.listener;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import ru.dargen.binaryprotocol.network.NetworkBound;
import ru.dargen.binaryprotocol.network.ProtocolPlayerConnection;
import ru.dargen.binaryprotocol.packet.BinaryBuf;
import ru.dargen.binaryprotocol.packet.type.PacketType;
import ru.dargen.binaryprotocol.util.MinecraftUtil;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PacketAdapter {

    private final Map<Integer, SortedSet<PacketListener>> listenerMap = new Int2ObjectArrayMap<>();

    public Set<PacketListener> getListeners(int packetTypeKey) {
        return listenerMap.get(packetTypeKey);
    }

    public Set<PacketListener> getListeners(PacketType type) {
        return getListeners(type.asKey());
    }

    public Iterator<PacketListener> getListenersIterator(int packetTypeKey) {
        return listenerMap.containsKey(packetTypeKey) ? getListeners(packetTypeKey).iterator() : null;
    }

    public Iterator<PacketListener> getListenersIterator(PacketType type) {
        return getListenersIterator(type.asKey());
    }

    public void registerListener(PacketListener listener) {
        for (Integer packetTypeKey : listener.listenPacketTypes()) {
            var listenerSet = listenerMap.computeIfAbsent(packetTypeKey, key -> new TreeSet<>());
            listenerSet.add(listener);
        }
    }

    public void registerListener(PacketHandler handler, boolean async, PacketType... packetTypes) {
        registerListener(new PacketListener(
                handler, Arrays.stream(packetTypes).map(PacketType::asKey).collect(Collectors.toSet()), async));
    }

    public void registerListener(PacketHandler handler, PacketType... packetTypes) {
        registerListener(handler, true, packetTypes);
    }

    public void unregisterListener(PacketListener listener) {
        for (Integer packetTypeKey : listener.listenPacketTypes()) {
            var listenerSet = listenerMap.get(packetTypeKey);
            if (listenerSet == null) {
                continue;
            }

            listenerSet.remove(listener);
            if (listenerSet.isEmpty()) {
                listenerMap.remove(packetTypeKey);
            }
        }
    }

    public void clearListeners(int packetTypeKey) {
        listenerMap.remove(packetTypeKey);
    }

    public void clearListeners(PacketType packetType) {
        clearListeners(packetType.asKey());
    }

    public BinaryBuf processPacket(ProtocolPlayerConnection connection, int packetId, NetworkBound bound,
                                   BinaryBuf packet, Consumer<BinaryBuf> successHandler) {
        var packetTypeKey = PacketType.toKey(packetId, bound, connection.getPhase());

        System.out.printf("In packet %s %s with typeId %s handlers: %s%n", packetId, bound, packetTypeKey, listenerMap.values());
        var listeners = getListenersIterator(packetTypeKey);
        if (listeners == null) {
            return packet;
        }

        var packetType = new PacketType(packetId, bound, connection.getPhase());
        var packetContext = new PacketContext(connection, packetType, packet);

        PacketListener listener;
        while (listeners.hasNext()) {
            listener = listeners.next();
            if (!listener.async()) {
                processPacketSync(packetContext, listeners, listener, successHandler);
                return null;
            }

            listener.process(packetContext);
        }

        return packetContext.getResultPacket();
    }

    private void processPacketSync(PacketContext packetContext, Iterator<PacketListener> iterator,
                                   PacketListener lastListener, Consumer<BinaryBuf> successHandler) {
        MinecraftUtil.postToMainThread(() -> {
            lastListener.process(packetContext);
            iterator.forEachRemaining(listener -> listener.process(packetContext));

            var resultPacket = packetContext.getResultPacket();
            if (resultPacket != null) {
                successHandler.accept(resultPacket);
            }
        });
    }

}
