package com.wlufei.rpc.framework.server;

import com.wlufei.rpc.framework.config.RpcServiceConfig;

public interface RPCServer {
    void registryServices(RpcServiceConfig rpcServiceConfig);

    void start();
}
