package com.wlufei.rpc.framework.loadbalance;


import com.wlufei.rpc.framework.common.RPCRequest;
import com.wlufei.rpc.framework.common.annotation.SPI;
import com.wlufei.rpc.framework.loadbalance.impl.RandomLoadBalanceImpl;

import java.util.List;

/**
 * 负载平衡
 *
 * @author labu
 * @date 2021/07/29
 */
@SPI(RandomLoadBalanceImpl.LOAD_BALANCE_RANDOM)
public interface LoadBalance {

    String whichOne(List<String> hostAddress, RPCRequest rpcRequest);
}
