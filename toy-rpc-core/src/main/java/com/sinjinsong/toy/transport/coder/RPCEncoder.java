package com.sinjinsong.toy.transport.coder;

import com.sinjinsong.toy.common.util.ProtostuffUtil;
import com.sinjinsong.toy.transport.domain.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by SinjinSong on 2017/7/30.
 */
@Slf4j
public class RPCEncoder extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        Message message = (Message) msg;
        out.writeByte((message.getType()));
        if (message.getType() == Message.REQUEST) {
            out.writeBytes(ProtostuffUtil.serialize(message.getRequest()));
        }
        if (message.getType() == Message.RESPONSE) {
            out.writeBytes(ProtostuffUtil.serialize(message.getResponse()));
        }
    }
}
