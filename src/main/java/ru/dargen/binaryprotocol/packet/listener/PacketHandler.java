package ru.dargen.binaryprotocol.packet.listener;

@FunctionalInterface
public interface PacketHandler {

    void handle(PacketContext context);

}
