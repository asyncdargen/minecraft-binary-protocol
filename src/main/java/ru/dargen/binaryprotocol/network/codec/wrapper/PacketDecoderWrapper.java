package ru.dargen.binaryprotocol.network.codec.wrapper;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.concurrent.FastThreadLocal;
import ru.dargen.crowbar.util.MethodHandles;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
public interface PacketDecoderWrapper {

    MethodHandle DECODE_MH = MethodHandles.findMethod(
            ByteToMessageDecoder.class, "decode", void.class,
            ChannelHandlerContext.class, ByteBuf.class, List.class);
    FastThreadLocal<ArrayList<Object>> OUT_THREAD_LOCAL = new FastThreadLocal<>() {
        @Override
        protected ArrayList<Object> initialValue() throws Exception {
            return new ArrayList<>(1);
        }
    };

    static PacketDecoderWrapper wrap(ChannelHandler handler) {
        return MethodHandles.asProxy(PacketDecoderWrapper.class, DECODE_MH.bindTo(handler));
    }

    default Object decode(ChannelHandlerContext ctx, ByteBuf buf) {
        var out = OUT_THREAD_LOCAL.get();
        decode(ctx, buf, out);
        return out.isEmpty() ? null : out.remove(0);
    }

    void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out);

}
