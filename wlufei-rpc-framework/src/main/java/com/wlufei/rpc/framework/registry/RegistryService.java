package com.wlufei.rpc.framework.registry;


import com.wlufei.rpc.framework.common.RPCRequest;
import com.wlufei.rpc.framework.common.annotation.SPI;

import java.net.InetSocketAddress;

/**
 * 注册服务
 *
 * @author labu
 * @date 2021/07/28
 */
@SPI
public interface RegistryService {

    /**
     * 注册表服务
     * 注册服务
     *
     * @param rpcServiceName    rpc服务名称
     * @param inetSocketAddress inet套接字地址
     */
    void registryService(String rpcServiceName, InetSocketAddress inetSocketAddress);

    /**
     * 查找服务
     *
     * @param rpcRequest rpc请求
     * @return {@link InetSocketAddress}
     */
    InetSocketAddress lookupService(RPCRequest rpcRequest);
}
