package ru.dargen.binaryprotocol.network.codec.wrapper;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import ru.dargen.crowbar.util.MethodHandles;

import java.lang.invoke.MethodHandle;

@FunctionalInterface
public interface PacketEncoderWrapper {

    MethodHandle ENCODE_MH = MethodHandles.findMethod(
            MessageToByteEncoder.class, "encode", void.class,
            ChannelHandlerContext.class, Object.class, ByteBuf.class);

    static PacketEncoderWrapper wrap(ChannelHandler handler) {
        return MethodHandles.asProxy(PacketEncoderWrapper.class, ENCODE_MH.bindTo(handler));
    }

    default ByteBuf encode(ChannelHandlerContext context, Object packet) {
        var buf = context.alloc().ioBuffer();
        encode(context, packet, buf);
        return buf;
    }

    void encode(ChannelHandlerContext ctx, Object packet, ByteBuf buf);

}
