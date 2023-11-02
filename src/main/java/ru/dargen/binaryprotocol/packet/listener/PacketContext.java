package ru.dargen.binaryprotocol.packet.listener;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;
import ru.dargen.binaryprotocol.network.ProtocolPlayerConnection;
import ru.dargen.binaryprotocol.packet.BinaryBuf;
import ru.dargen.binaryprotocol.packet.BinaryPacket;
import ru.dargen.binaryprotocol.packet.type.PacketType;

import java.util.function.Consumer;

@Getter @Setter
@RequiredArgsConstructor
public class PacketContext {

    private final ProtocolPlayerConnection connection;
    private final PacketType type;
    private final BinaryBuf packet;

    @Setter(AccessLevel.PRIVATE)
    private BinaryBuf replaced = null;
    private boolean isCancelled = false;

    public Player getPlayer() {
        return connection.getPlayer();
    }

    public <P extends BinaryPacket> P read(P packet) {
        packet.read(this.packet);
        return packet;
    }

    public BinaryBuf replace() {
        var buffer = connection.alloc();
        buffer.writeVarInt(type.id());
        return replaced = buffer;
    }

    public BinaryBuf replace(BinaryPacket packet) {
        return replaced = packet.build(connection.alloc());
    }

    public BinaryBuf replace(Consumer<BinaryBuf> builder) {
        var buffer = replace();
        builder.accept(buffer);
        return buffer;
    }

    public boolean isReplaced() {
        return replaced != null;
    }

    public void cancel() {
        isCancelled = true;
    }

    public BinaryBuf getResultPacket() {
        return isCancelled() ? null : isReplaced() ? replaced : packet;
    }

}
