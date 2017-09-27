package cn.sinjinsong.rpc.core.client;

import cn.sinjinsong.rpc.core.coder.RPCDecoder;
import cn.sinjinsong.rpc.core.coder.RPCEncoder;
import cn.sinjinsong.rpc.core.domain.RPCRequest;
import cn.sinjinsong.rpc.core.domain.RPCResponse;
import cn.sinjinsong.rpc.core.domain.RPCResponseFuture;
import cn.sinjinsong.rpc.core.enumeration.MessageType;
import cn.sinjinsong.rpc.core.zookeeper.ServiceRegistry;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by SinjinSong on 2017/7/29.
 */
@Slf4j
public class RPCClient {
    private EventLoopGroup group;
    private Bootstrap bootstrap;
    private Channel futureChannel;
    private Map<String, RPCResponseFuture> responses;
    private ServiceRegistry registry;

    public RPCClient() {
        log.info("初始化RPC客户端");
        this.responses = new ConcurrentHashMap<>();
        this.registry = new ServiceRegistry();
        this.group = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(group).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline()
                                .addLast(new IdleStateHandler(0, 0, 5))
                                .addLast(new RPCEncoder(RPCRequest.class)) // 将 RPC 请求进行编码（为了发送请求）
                                .addLast(new RPCDecoder(RPCResponse.class)) // 将 RPC 响应进行解码（为了处理响应）
                                .addLast(new RPCClientHandler(responses)); // 使用 RpcClient 发送 RPC 请求
                    }
                })
                .option(ChannelOption.SO_KEEPALIVE, true);
        this.connect();
        log.info("客户端初始化完毕");
    }


    private void connect() {
        try {
            ChannelFuture future = bootstrap.connect("127.0.0.1", 8080).sync();
            this.futureChannel = future.channel();
            log.info("客户端已连接");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void register(String address) {
        log.info("客户端向Zookeeper注册完毕");
    }
    
    /**
     * 关闭连接
     */
    public void close() {
        try {
            this.futureChannel.close().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public RPCResponseFuture execute(RPCRequest request) throws Exception {
        if (!futureChannel.isActive()) {
            connect();
        }
        log.info("客户端发起请求: {}", request);
        RPCResponseFuture responseFuture = new RPCResponseFuture();
        responses.put(request.getRequestId(), responseFuture);
        this.futureChannel.writeAndFlush(request);
        log.info("请求已发送");
        return responseFuture;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<?> interfaceClass) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        RPCRequest request = new RPCRequest(); // 创建并初始化 RPC 请求
                        log.info("调用远程服务：{} {}", method.getDeclaringClass().getName(), method.getName());
                        request.setRequestId(UUID.randomUUID().toString());
                        request.setClassName(method.getDeclaringClass().getName());
                        request.setMethodName(method.getName());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setParameters(args);
                        request.setType(MessageType.NORMAL);
                        RPCResponseFuture responseFuture = RPCClient.this.execute(request); // 通过 RPC 客户端发送 RPC 请求并获取 RPC 响应
                        RPCResponse response = responseFuture.getResponse();
                        log.info("客户端读到响应");
                        if (response.hasError()) {
                            throw response.getCause();
                        } else {
                            return response.getResult();
                        }
                    }
                }
        );
    }
}
