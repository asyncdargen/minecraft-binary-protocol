package ru.dargen.binaryprotocol;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.dargen.binaryprotocol.network.ProtocolInjector;
import ru.dargen.binaryprotocol.packet.listener.PacketAdapter;

import java.util.Collection;
import java.util.logging.Logger;

@UtilityClass
public class BinaryProtocol {

    public static final Logger LOGGER = Logger.getLogger("BinaryProtocol");

    private final PacketAdapter ADAPTER = new PacketAdapter();
    private final ProtocolInjector INJECTOR = new ProtocolInjector(ADAPTER);

    public void sendPacket(Player player, Object packet) {
        INJECTOR.getConnection(player).writePacket(packet);
    }

    public void receivePacket(Player player, Object packet) {
        INJECTOR.getConnection(player).receivePacket(packet);
    }

    public void sendPacket(Collection<? extends Player> players, Object packet) {
        players.forEach(player -> sendPacket(player, packet));
    }

    public void receivePacket(Collection<? extends Player> players, Object packet) {
        players.forEach(player -> receivePacket(player, packet));
    }

    public void broadcastPacket(Object packet) {
        sendPacket(Bukkit.getOnlinePlayers(), packet);
    }

    public void broadcastReceivePacket(Object packet) {
        receivePacket(Bukkit.getOnlinePlayers(), packet);
    }

    public PacketAdapter adapter() {
        return ADAPTER;
    }

    public ProtocolInjector injector() {
        return INJECTOR;
    }

    public void init() {
        //only for classloading
    }

}
