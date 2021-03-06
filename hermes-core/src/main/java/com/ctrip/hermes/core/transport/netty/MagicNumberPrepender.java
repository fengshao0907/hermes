package com.ctrip.hermes.core.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MagicNumberPrepender extends MessageToByteEncoder<ByteBuf> {

	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
		Magic.writeMagic(out);

		out.writeBytes(msg, msg.readerIndex(), msg.readableBytes());
	}

}
