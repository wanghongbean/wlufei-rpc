package com.wlufei.rpc.framework.server.impl;


import com.wlufei.rpc.framework.common.utils.ThreadPoolFactoryUtils;
import com.wlufei.rpc.framework.config.CustomShutdownHook;
import com.wlufei.rpc.framework.config.RpcServiceConfig;
import com.wlufei.rpc.framework.provider.ServiceProvider;
import com.wlufei.rpc.framework.provider.impl.ServiceProviderImpl;
import com.wlufei.rpc.framework.server.RPCServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * 套接字rpc server 实现
 *
 * @author labu
 * @date 2021/07/29
 */
@Slf4j
public class SocketRPCServer implements RPCServer {
    private final ExecutorService threadPool;
    private final ServiceProvider serviceProvider;

    public SocketRPCServer() {
        threadPool = ThreadPoolFactoryUtils.createCustomThreadPoolIfAbsent("WLF-RPC-SOCKET");
        serviceProvider = new ServiceProviderImpl();
    }


    @Override
    public void registryServices(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }

    @Override
    public void start() {
        try {
            CustomShutdownHook.getCustomShutdownHook().clearAll();
            ServerSocket server = new ServerSocket();
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            server.bind(new InetSocketAddress(hostAddress, PORT));
            while (true) {
                Socket socket = server.accept();
                log.info("client connected:{}", socket.getInetAddress());
                //接收socket后,解析处理发现服务,调用任务,返回结果到client
                threadPool.execute(new SocketRPCRequestRunnable(socket));
                if (null == socket) {
                    break;
                }
            }
            threadPool.shutdown();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
