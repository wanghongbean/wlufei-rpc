package com.wlufei.rpc.framework.server;

import com.wlufei.rpc.framework.config.RpcServiceConfig;

public interface RPCServer {
    int PORT = 8888;
    void registryServices(RpcServiceConfig rpcServiceConfig);

    void start();
}
