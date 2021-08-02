package com.wlufei.rpc.framework.registry;


import com.wlufei.rpc.framework.common.RPCRequest;

import java.net.InetSocketAddress;

/**
 * 服务发现
 *
 * @author labu
 * @date 2021/07/29
 */
@Deprecated//统一由RegistryService提供
public interface ServiceDiscovery {

    /**
     * 查找服务
     *
     * @param rpcRequest rpc请求
     * @return {@link InetSocketAddress}
     */
    InetSocketAddress lookupService(RPCRequest rpcRequest);
}
