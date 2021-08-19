package com.wlufei.rpc.framework.config;

import com.wlufei.rpc.framework.common.utils.CuratorUtils;
import com.wlufei.rpc.framework.common.utils.ThreadPoolFactoryUtils;
import com.wlufei.rpc.framework.server.RPCServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook(){
        return CUSTOM_SHUTDOWN_HOOK;
    }
    public void clearAll(){
        log.info("shutdownHook for clear all");
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), RPCServer.PORT);
                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(),inetSocketAddress);
            } catch (UnknownHostException e) {

            }
            ThreadPoolFactoryUtils.shutDownAllThreadPool();
        }));
    }
}
