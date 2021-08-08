package com.wlufei.rpc.framework.registry.impl;


import com.wlufei.rpc.framework.common.RPCRequest;
import com.wlufei.rpc.framework.common.URL;
import com.wlufei.rpc.framework.common.enums.RpcErrorMessageEnum;
import com.wlufei.rpc.framework.common.exception.RpcException;
import com.wlufei.rpc.framework.common.utils.CuratorUtils;
import com.wlufei.rpc.framework.loadbalance.LoadBalance;
import com.wlufei.rpc.framework.loadbalance.impl.RandomLoadBalanceImpl;
import com.wlufei.rpc.framework.registry.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * zk 服务发现 impl
 *
 * @author labu
 * @date 2021/07/29
 */
@Slf4j
@Deprecated
public class ZKServiceDiscoveryImpl implements ServiceDiscovery {
    private final LoadBalance loadBalance;

    public ZKServiceDiscoveryImpl() {
        loadBalance = new RandomLoadBalanceImpl();
    }

    @Override
    public InetSocketAddress lookupService(RPCRequest rpcRequest) {
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> childrenNodes = CuratorUtils.getChildrenNodes(zkClient, rpcRequest.getRPCServiceName());
        if (null == childrenNodes || childrenNodes.isEmpty()) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcRequest.getRPCServiceName());
        }
        URL url = URL.valueOf("");
        String targetServiceAddress = loadBalance.whichOne(childrenNodes, url, rpcRequest);
        log.info("lookup service success.serviceName:{},server:{}", rpcRequest.getRPCServiceName(), targetServiceAddress);
        String[] socketAddressArray = targetServiceAddress.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
