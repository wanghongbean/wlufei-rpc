package com.wlufei.rpc.framework.loadbalance.impl;


import com.wlufei.rpc.framework.common.RPCRequest;
import com.wlufei.rpc.framework.common.enums.RpcErrorMessageEnum;
import com.wlufei.rpc.framework.common.exception.RpcException;
import com.wlufei.rpc.framework.loadbalance.LoadBalance;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

/**
 * 随机 负载平衡 impl
 *
 * @author labu
 * @date 2021/07/29
 */
@Slf4j
public class RandomLoadBalanceImpl implements LoadBalance {

    @Override
    public String whichOne(List<String> hostAddress, RPCRequest rpcRequest) {
        if (null == hostAddress || hostAddress.isEmpty()) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcRequest.getRPCServiceName());
        }
        if (hostAddress.size() == 1) {
            return hostAddress.get(0);
        }
        return hostAddress.get(new Random().nextInt(hostAddress.size()));
    }
}
