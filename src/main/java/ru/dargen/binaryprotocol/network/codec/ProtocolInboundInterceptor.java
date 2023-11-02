package ru.dargen.binaryprotocol.network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.dargen.binaryprotocol.network.NetworkBound;
import ru.dargen.binaryprotocol.network.ProtocolPlayerConnection;
import ru.dargen.binaryprotocol.network.codec.wrapper.PacketDecoderWrapper;
import ru.dargen.binaryprotocol.packet.BinaryBuf;
import ru.dargen.binaryprotocol.network.NetworkPhase;
import ru.dargen.binaryprotocol.packet.listener.PacketAdapter;
import ru.dargen.binaryprotocol.packet.type.PacketTypes;

@RequiredArgsConstructor
public class ProtocolInboundInterceptor extends ChannelInboundHandlerAdapter {

    private final ProtocolPlayerConnection connection;
    private final PacketAdapter packetAdapter;

    private final PacketDecoderWrapper decoder;

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        connection.setDecoderChannelContext(ctx);

        if (msg instanceof ByteBuf packetBuf) {
            var packet = new BinaryBuf(packetBuf.asReadOnly());
            packet.markReaderIndex();

            var packetId = packet.readVarInt();

            //on login setting player name
            if (connection.getPhase() == NetworkPhase.LOGIN && packetId == PacketTypes.Login.In.START.id()) {
                var readerIndex = packet.readerIndex();
                connection.setPlayerName(packet.readString());
                packet.readerIndex(readerIndex);
            }

            var processedPacket = packetAdapter.processPacket(
                    connection, packetId, NetworkBound.SERVER,
                    packet, buffer -> ctx.channel().eventLoop().execute(() -> processPacket(ctx, buffer.asByteBuf())));

            if (processedPacket != null) {
                packetBuf = processedPacket.asByteBuf();
            } else return;

            processPacket(ctx, packetBuf);
        }
    }

    private void processPacket(ChannelHandlerContext ctx, ByteBuf buf) {
        buf.resetReaderIndex();
        var packet = decoder.decode(ctx, buf);
        ctx.fireChannelRead(packet);
    }

}
