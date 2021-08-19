package com.wlufei.rpc.framework.server.impl;

import com.wlufei.rpc.framework.common.utils.ThreadPoolFactoryUtils;
import com.wlufei.rpc.framework.config.CustomShutdownHook;
import com.wlufei.rpc.framework.config.RpcServiceConfig;
import com.wlufei.rpc.framework.provider.ServiceProvider;
import com.wlufei.rpc.framework.provider.impl.ServiceProviderImpl;
import com.wlufei.rpc.framework.server.RPCServer;
import com.wlufei.rpc.framework.server.handler.NettyRPCServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import sun.rmi.runtime.RuntimeUtil;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyRPCServer implements RPCServer {

    private final ServiceProvider serviceProvider;

    public NettyRPCServer() {
        this.serviceProvider = new ServiceProviderImpl();
    }

    @Override
    public void registryServices(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }

    @Override
    @SneakyThrows
    public void start() {
        CustomShutdownHook.getCustomShutdownHook().clearAll();
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        DefaultEventExecutorGroup serverHandlerGroup = new DefaultEventExecutorGroup(4, ThreadPoolFactoryUtils.createThreadFactory("NETTY-SERVER-HANDLER-", false));
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    //TCP默认开启了Nagle算法，该算法的作用是尽可能的发送大数据块，减少网络传输。
                    //TCP_NODELAY 参数的作用就是控制是否启用Nagle算法
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    //开启TCP底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    //表示系统用于临时存放已完成三次握手的请求队列的最大长度，如果连接建立频繁，服务器处理创建新连接较慢，可调大这个参数
                    .option(ChannelOption.SO_BACKLOG,128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    //当客户端第一次进行请求的时候才会进行初始化
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            //30秒之内没有收到客户端请求的话就关闭链接
                            p.addLast(new IdleStateHandler(30,0,0, TimeUnit.SECONDS));
                            p.addLast(serverHandlerGroup,new NettyRPCServerHandler());
                            //todo message encoder and decoder
                        }
                    });

            //绑定端口，同步等待绑定成功
            ChannelFuture cf = bootstrap.bind(hostAddress, PORT).sync();
            //等待服务端监听端口关闭
            cf.channel().closeFuture().sync();

        }catch (InterruptedException e){
           log.error("occur exception when start server: ",e);
        }finally {
            log.error("shutdown ...");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serverHandlerGroup.shutdownGracefully();
        }


    }
}
