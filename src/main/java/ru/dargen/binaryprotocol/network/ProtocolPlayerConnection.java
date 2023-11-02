package ru.dargen.binaryprotocol.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.dargen.binaryprotocol.BinaryProtocol;
import ru.dargen.binaryprotocol.TestPlugin;
import ru.dargen.binaryprotocol.network.codec.wrapper.PacketDecoderWrapper;
import ru.dargen.binaryprotocol.packet.BinaryBuf;
import ru.dargen.binaryprotocol.packet.BinaryPacket;
import ru.dargen.crowbar.util.ClassLoaders;
import ru.dargen.crowbar.util.Reflection;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@RequiredArgsConstructor
public class ProtocolPlayerConnection {

    public static final AttributeKey<ProtocolPlayerConnection> PROTOCOL_CONNECTION_ATTRIBUTE = AttributeKey.newInstance("protocol_connection");
    public static final AttributeKey<Enum<?>> CONNECTION_STATE_ATTRIBUTE = AttributeKey.valueOf("protocol");

    @Getter
    private final Channel channel;
    private NetworkPhase phase;

    @Setter
    private ChannelHandlerContext decoderChannelContext;
    private final PacketDecoderWrapper packetDecoder;

    private String playerName;
    private Player player;

    public String getPlayerName() {
        return playerName == null ? "HandshakingPlayer" : playerName;
    }

    public void setPlayerName(String playerName) {
        BinaryProtocol.injector().getPlayerConnectionMap().put(playerName.toLowerCase(), this);
        this.playerName = playerName;
    }

    public Player getPlayer() {
        if (phase == NetworkPhase.PLAY && (player == null || player instanceof ProtocolTemporalPlayer)) {
            val bukkitPlayer = Bukkit.getPlayer(playerName);
            if (bukkitPlayer != null) {
                player = bukkitPlayer;
            }
        }

        if (player == null) {
            player = ProtocolTemporalPlayer.create(this);
        }

        return player;
    }

    public NetworkPhase getPhase() {
        if (phase == NetworkPhase.PLAY) {
            return phase;
        }

        return phase = NetworkPhase.values()[channel.attr(CONNECTION_STATE_ATTRIBUTE).get().ordinal()];
    }

    public void writePacket(Object packet, boolean inEventLoop) {
        if (inEventLoop && !channel.eventLoop().inEventLoop()) {
            channel.eventLoop().execute(() -> channel.writeAndFlush(packet));
        } else channel.writeAndFlush(packet);
    }

    public void writePacket(Object packet) {
        writePacket(packet, true);
    }

    private void receivePacket0(Object packet) {
        if (packet instanceof BinaryPacket binaryPacket) {
            packet = binaryPacket.build(alloc()).asByteBuf();
        } else if (packet instanceof BinaryBuf buf) {
            packet = buf.asByteBuf();
        }

        if (packet instanceof ByteBuf buf) {
            packet = packetDecoder.decode(decoderChannelContext, buf);
        }

        decoderChannelContext.fireChannelRead(packet);
    }

    public void receivePacket(Object packet, boolean inEventLoop) {
        if (inEventLoop && !channel.eventLoop().inEventLoop()) {
            channel.eventLoop().execute(() -> receivePacket0(packet));
        } else receivePacket0(packet);
    }

    public void receivePacket(Object packet) {
        receivePacket(packet, true);
    }

    public BinaryBuf alloc() {
        return BinaryBuf.alloc(channel.alloc());
    }

    interface ProtocolTemporalPlayer {

        Method PLAYER_NAME_METHOD = Reflection.getMethod(Player.class, "getName");

        static Player create(ProtocolPlayerConnection connection) {
            return (Player) Proxy.newProxyInstance(
                    ClassLoaders.of(TestPlugin.class), //TODO: Classloaders.classLoader();
                    new Class[]{Player.class, ProtocolTemporalPlayer.class},
                    (instance, method, args) -> {
                        if (method == PLAYER_NAME_METHOD) {
                            return connection.getPlayerName();
                        }

                        throw new IllegalStateException("ProtocolTemporalPlayer not supports " + method);
                    });
        }

    }

}
