package com.sinjinsong.rpc.core.client.handler;

import com.sinjinsong.rpc.core.client.call.callback.CallbackCallHandler;
import com.sinjinsong.rpc.core.client.context.RPCThreadSharedContext;
import com.sinjinsong.rpc.core.client.endpoint.Endpoint;
import com.sinjinsong.rpc.core.domain.Message;
import com.sinjinsong.rpc.core.domain.RPCRequest;
import com.sinjinsong.rpc.core.domain.RPCResponse;
import com.sinjinsong.rpc.core.server.invoker.RPCInvoker;
import com.sinjinsong.rpc.core.server.wrapper.HandlerWrapper;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static com.sinjinsong.rpc.core.domain.Message.*;

/**
 * Created by SinjinSong on 2017/7/31.
 */
@Slf4j
@ChannelHandler.Sharable
@AllArgsConstructor
public class RPCClientHandler extends SimpleChannelInboundHandler<Message> {
    private Endpoint endpoint;
    private ThreadPoolExecutor pool;
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("客户端捕获到异常");
        cause.printStackTrace();
        log.info("与服务器{} 的连接断开", endpoint.getAddress());
        endpoint.handleException();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端与服务器{}通道已开启...", endpoint.getAddress());
    }

    /**
     * 当超过规定时间，客户端未读写数据，那么会自动调用userEventTriggered方法，向服务器发送一个心跳包
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            log.info("超过指定时间未发送数据，客户端主动发送心跳信息至{}", endpoint.getAddress());
            ctx.writeAndFlush(Message.PING_MSG);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        log.info("接收到服务器 {} 响应: {}", endpoint.getAddress(), message);
        //服务器不会PING客户端
        if (message.getType() == PONG) {
            log.info("收到服务器的PONG心跳响应");
        } else if (message.getType() == RESPONSE) {
            RPCResponse response = message.getResponse();
            CompletableFuture<RPCResponse> future = RPCThreadSharedContext.getAndRemoveResponseFuture(response.getRequestId());
            future.complete(response);
        } else if (message.getType() == REQUEST) {
            // callback
            RPCRequest request = message.getRequest();
            pool.submit(new RPCInvoker(ctx, request,
                    new HandlerWrapper(RPCThreadSharedContext.getAndRemoveHandler(
                            CallbackCallHandler.generateCallbackHandlerKey(request)
                    )))
            );
        }
    }
}
