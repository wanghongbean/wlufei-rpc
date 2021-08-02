package com.wlufei.rpc.demo.client;

import com.wlufei.rpc.demo.api.GoingMerry;
import com.wlufei.rpc.demo.api.WangLufeiService;
import com.wlufei.rpc.framework.client.SocketRPCClient;
import com.wlufei.rpc.framework.config.RpcServiceConfig;
import com.wlufei.rpc.framework.proxy.RPCClientProxy;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @Author labu
 * @Date 2021/7/29
 * @Description
 */
public class SocketRpcClientDemo {
    public static void main(String[] args) throws InterruptedException {
        SocketRPCClient socketRPCClient = new SocketRPCClient();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        RPCClientProxy rpcClientProxy = new RPCClientProxy(socketRPCClient, rpcServiceConfig);
        WangLufeiService wangLufeiService = rpcClientProxy.getProxyInstance(WangLufeiService.class);
        GoingMerry goingMerry = GoingMerry.builder().captain("lufei")
                .partners(Arrays.asList("suolong", "namei", "wusuopu", "qiaoba")).build();
        String result = wangLufeiService.onePiece(goingMerry);
        System.out.println("======   "+ result);
        TimeUnit.SECONDS.sleep(2);
        GoingMerry baji = GoingMerry.builder().captain("baji").build();
        String bajiResult = wangLufeiService.onePiece(baji);
        System.out.println(bajiResult);

    }
}
