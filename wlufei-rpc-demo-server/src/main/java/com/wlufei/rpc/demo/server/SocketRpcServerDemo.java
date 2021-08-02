package com.wlufei.rpc.demo.server;


import com.wlufei.rpc.demo.api.WangLufeiService;
import com.wlufei.rpc.demo.service.WangLuFeiServiceImpl;
import com.wlufei.rpc.framework.config.RpcServiceConfig;
import com.wlufei.rpc.framework.server.impl.SocketRPCServer;

/**
 * socket rpc 服务器演示
 *
 * @author labu
 * @date 2021/07/29
 */
public class SocketRpcServerDemo {
    public static void main(String[] args) {
        WangLufeiService wangLuFeiService = new WangLuFeiServiceImpl();
        SocketRPCServer socketRPCServer = new SocketRPCServer();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        rpcServiceConfig.setTargetService(wangLuFeiService);
        socketRPCServer.registryServices(rpcServiceConfig);
        socketRPCServer.start();
    }
}
