package com.wlufei.rpc.framework.registry.impl;

import com.wlufei.rpc.framework.common.RPCRequest;
import com.wlufei.rpc.framework.common.URL;
import com.wlufei.rpc.framework.common.enums.RpcErrorMessageEnum;
import com.wlufei.rpc.framework.common.exception.RpcException;
import com.wlufei.rpc.framework.common.utils.CuratorUtils;
import com.wlufei.rpc.framework.loadbalance.LoadBalance;
import com.wlufei.rpc.framework.registry.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;


/**
 * impl zk 实现服务注册中心
 *
 * @author labu
 * @date 2021/07/28
 */
@Slf4j
public class ZKRegistryServiceImpl implements RegistryService {
    public static final String ZK_REGISTRY_SPI = "zkRegistry";

    private LoadBalance loadBalance;

    public ZKRegistryServiceImpl() {
//        loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(RandomLoadBalanceImpl.LOAD_BALANCE_RANDOM);
    }

    public void setLoadBalance(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    @Override
    public void registryService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient, servicePath);
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
