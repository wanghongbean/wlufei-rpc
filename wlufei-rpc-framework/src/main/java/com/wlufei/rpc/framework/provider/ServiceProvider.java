package com.wlufei.rpc.framework.provider;


import com.wlufei.rpc.framework.config.RpcServiceConfig;

/**
 * 服务提供者
 *
 * @author labu
 * @date 2021/07/28
 */
public interface ServiceProvider {

    /**
     * 添加注册服务
     *
     * @param rpcServiceConfig rpc服务配置
     */
    void addService(RpcServiceConfig rpcServiceConfig);

    /**
     * 发现服务
     *
     * @param rpcServiceConfig rpc服务配置
     */
    Object getService(String rpcServiceName);

    /**
     * 发布服务
     *
     * @param rpcServiceConfig rpc服务配置
     */
    void publishService(RpcServiceConfig rpcServiceConfig);

}
