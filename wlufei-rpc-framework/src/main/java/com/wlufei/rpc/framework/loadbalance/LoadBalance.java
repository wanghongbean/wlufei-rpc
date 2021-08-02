package com.wlufei.rpc.framework.loadbalance;


import com.wlufei.rpc.framework.common.RPCRequest;

import java.util.List;

/**
 * 负载平衡
 *
 * @author labu
 * @date 2021/07/29
 */
public interface LoadBalance {

    String whichOne(List<String> hostAddress, RPCRequest rpcRequest);
}