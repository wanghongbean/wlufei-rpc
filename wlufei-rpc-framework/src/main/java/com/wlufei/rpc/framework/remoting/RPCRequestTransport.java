package com.wlufei.rpc.framework.remoting;

import com.wlufei.rpc.framework.common.RPCRequest;

/**
 * rpc request 传输
 *
 * @author labu
 * @date 2021/07/29
 */
public interface RPCRequestTransport {

    Object sendRPCRequest(RPCRequest rpcRequest);
}
