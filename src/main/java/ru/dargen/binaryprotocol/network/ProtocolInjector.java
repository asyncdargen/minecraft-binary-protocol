package ru.dargen.binaryprotocol.network;

import io.netty.channel.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.dargen.binaryprotocol.BinaryProtocol;
import ru.dargen.binaryprotocol.network.codec.ProtocolInboundInterceptor;
import ru.dargen.binaryprotocol.network.codec.ProtocolOutboundInterceptor;
import ru.dargen.binaryprotocol.network.codec.wrapper.PacketDecoderWrapper;
import ru.dargen.binaryprotocol.network.codec.wrapper.PacketEncoderWrapper;
import ru.dargen.binaryprotocol.packet.listener.PacketAdapter;
import ru.dargen.binaryprotocol.util.MinecraftUtil;
import ru.dargen.crowbar.Accessors;
import ru.dargen.crowbar.accessor.MethodAccessor;
import ru.dargen.crowbar.util.Reflection;
import ru.dargen.crowbar.util.Unsafe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.dargen.binaryprotocol.network.ProtocolPlayerConnection.PROTOCOL_CONNECTION_ATTRIBUTE;

@RequiredArgsConstructor
public class ProtocolInjector {

    private final PacketAdapter packetAdapter;

    @Getter
    private final Map<String, ProtocolPlayerConnection> playerConnectionMap = new HashMap<>();

    public ProtocolPlayerConnection getConnection(String playerName) {
        return playerConnectionMap.get(playerName.toLowerCase());
    }

    public ProtocolPlayerConnection getConnection(Player player) {
        return getConnection(player.getName());
    }

    public void inject() {
        var serverConnection = Reflection.invokeMethod(
                Reflection.getMethod(MinecraftUtil.MINECRAFT_SERVER_CLASS, "ad"), MinecraftUtil.MINECRAFT_SERVER);
        var serverChannelsList = Reflection.<List<ChannelFuture>>getFieldValue(
                Reflection.getField(serverConnection, "f"),
                serverConnection);

        serverChannelsList.forEach(future -> injectServerChannel(future.channel()));
        BinaryProtocol.LOGGER.info("Injected!");
    }

    @SuppressWarnings("unchecked")
    private void injectServerChannel(Channel channel) {
        var bootstrapAcceptorClass = (Class<ChannelHandler>)
                Reflection.getClass("io.netty.bootstrap.ServerBootstrap$ServerBootstrapAcceptor");
        var bootstrapChannelAcceptor = channel.pipeline().get(bootstrapAcceptorClass);

        var childHandlerField = Reflection.getField(bootstrapAcceptorClass, "childHandler");
        var originInitializer = Reflection.<ChannelInitializer<Channel>>getFieldValue(childHandlerField, bootstrapChannelAcceptor);
        Unsafe.setFieldValue(childHandlerField, bootstrapChannelAcceptor, new ChannelInjectedInitializer(originInitializer));
    }

    @RequiredArgsConstructor
    class ChannelInjectedInitializer extends ChannelInitializer<Channel> {

        private final static MethodAccessor<Void> INIT_CHANNEL_METHOD_ACCESSOR = Accessors.invoke().openMethod(
                ChannelInitializer.class, "initChannel", void.class, Channel.class);

        private final ChannelInitializer<Channel> originInitializer;

        @Override
        protected void initChannel(@NotNull Channel channel) throws Exception {
            INIT_CHANNEL_METHOD_ACCESSOR.invoke(originInitializer, channel);
            var pipeline = channel.pipeline();

            var encoder = PacketEncoderWrapper.wrap(pipeline.remove("encoder"));
            var decoder = PacketDecoderWrapper.wrap(pipeline.remove("decoder"));

            var connection = new ProtocolPlayerConnection(channel, decoder);
            channel.attr(PROTOCOL_CONNECTION_ATTRIBUTE).set(connection);

            pipeline.addAfter("splitter", "decoder",
                            new ProtocolInboundInterceptor(connection, packetAdapter, decoder))
                    .addAfter("prepender", "encoder",
                            new ProtocolOutboundInterceptor(connection, packetAdapter, encoder))
                    .addLast(new ChannelInactiveHandler());
        }

    }

    class ChannelInactiveHandler extends ChannelHandlerAdapter {

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            if (!ctx.channel().hasAttr(PROTOCOL_CONNECTION_ATTRIBUTE)) {
                return;
            }
            var connection = ctx.channel().attr(PROTOCOL_CONNECTION_ATTRIBUTE).get();

            var playerName = connection.getPlayerName();
            playerConnectionMap.remove(playerName.toLowerCase());
        }

    }


}
