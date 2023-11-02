package ru.dargen.binaryprotocol.packet.listener;

import org.jetbrains.annotations.NotNull;
import ru.dargen.binaryprotocol.BinaryProtocol;

import java.util.Set;
import java.util.logging.Level;

public record PacketListener(PacketHandler handler, Set<Integer> listenPacketTypes, boolean async) implements Comparable<PacketListener> {

    public void process(PacketContext context) {
        try {
            handler.handle(context);
        } catch (Throwable t) {
            BinaryProtocol.LOGGER.log(Level.SEVERE, "Error while processing sync packet" + context.getType(), t);
        }
    }

    @Override
    public String toString() {
        return "%sPacketListener(%s)".formatted(async ? "Async" : "Sync", listenPacketTypes);
    }

    @Override
    public int compareTo(@NotNull PacketListener o) {
        return Boolean.compare(o.async, async);
    }

}
