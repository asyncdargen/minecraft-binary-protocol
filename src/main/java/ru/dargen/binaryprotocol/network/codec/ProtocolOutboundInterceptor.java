package ru.dargen.binaryprotocol.network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.RequiredArgsConstructor;
import ru.dargen.binaryprotocol.network.NetworkBound;
import ru.dargen.binaryprotocol.network.ProtocolPlayerConnection;
import ru.dargen.binaryprotocol.network.codec.wrapper.PacketEncoderWrapper;
import ru.dargen.binaryprotocol.packet.BinaryBuf;
import ru.dargen.binaryprotocol.packet.BinaryPacket;
import ru.dargen.binaryprotocol.packet.listener.PacketAdapter;

@RequiredArgsConstructor
public class ProtocolOutboundInterceptor extends ChannelOutboundHandlerAdapter {

    private final ProtocolPlayerConnection connection;
    private final PacketAdapter packetAdapter;

    private final PacketEncoderWrapper encoder;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        var packetBuf = msg instanceof BinaryPacket packet ? packet.build(BinaryBuf.alloc(ctx.alloc())).asByteBuf()
                : msg instanceof BinaryBuf buffer ? buffer.asByteBuf()
                : msg instanceof ByteBuf buffer ? buffer
                : encoder.encode(ctx, msg);

        var packet = new BinaryBuf(packetBuf.asReadOnly());
        packet.markReaderIndex();

        var packetId = packet.readVarInt();
        var processedPacket = packetAdapter.processPacket(
                connection, packetId, NetworkBound.CLIENT, packet,
                buffer -> ctx.channel().eventLoop().execute(() -> processPacket(ctx, promise, buffer.asByteBuf())));

        if (processedPacket != null) {
            packetBuf = processedPacket.asByteBuf();
        } else return;

        processPacket(ctx, promise, packetBuf);
    }

    private void processPacket(ChannelHandlerContext ctx, ChannelPromise promise, ByteBuf buf) {
        buf.resetReaderIndex();
        ctx.write(buf, promise);
    }

}
