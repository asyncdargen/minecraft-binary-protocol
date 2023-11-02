package ru.dargen.binaryprotocol;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.dargen.binaryprotocol.network.NetworkBound;
import ru.dargen.binaryprotocol.network.NetworkPhase;
import ru.dargen.binaryprotocol.packet.type.PacketType;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static ru.dargen.binaryprotocol.packet.BinaryPacket.createPacket;

public class TestPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        BinaryProtocol.init();

        Bukkit.getPluginManager().registerEvents(this, this);
        BinaryProtocol.adapter().registerListener(ctx -> {
            ctx.getPlayer().sendMessage("On async " + Thread.currentThread().getName());
        }, new PacketType(0x31, NetworkBound.CLIENT, NetworkPhase.PLAY));

    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onJoin(PlayerChatEvent event) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            BinaryProtocol.sendPacket(event.getPlayer(), createPacket(0x01, buffer -> {
                buffer.writeVarInt(ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE));
                buffer.writeUUID(UUID.randomUUID());
                buffer.writeEntityType(EntityType.ARMOR_STAND);

                buffer.writeDouble(event.getPlayer().getLocation().getX());
                buffer.writeDouble(event.getPlayer().getLocation().getY());
                buffer.writeDouble(event.getPlayer().getLocation().getZ());

                buffer.writeByte(0);
                buffer.writeByte(0);
                buffer.writeByte(0);

                buffer.writeVarInt(0);

                buffer.writeShort(0);
                buffer.writeShort(0);
                buffer.writeShort(0);
            }));
        }, 1, 1);
    }
}
