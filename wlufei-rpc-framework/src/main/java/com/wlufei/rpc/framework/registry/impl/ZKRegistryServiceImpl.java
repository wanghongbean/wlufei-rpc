package com.wlufei.rpc.framework.registry.impl;

import com.wlufei.rpc.framework.common.RPCRequest;
import com.wlufei.rpc.framework.common.enums.RpcErrorMessageEnum;
import com.wlufei.rpc.framework.common.exception.RpcException;
import com.wlufei.rpc.framework.common.utils.CuratorUtils;
import com.wlufei.rpc.framework.loadbalance.LoadBalance;
import com.wlufei.rpc.framework.registry.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.Arrays;
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
//        v1.0实现方式
//        loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(RandomLoadBalanceImpl.LOAD_BALANCE_RANDOM);
    }

    public void setLoadBalance(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    @Override
    public void registryService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.clearRegistry(zkClient,rpcServiceName);//fix 客户端调用超时问题,原因是zk上注册了公司的IP相同的服务,随机的调用到无效的IP上导致超时
        CuratorUtils.createPersistentNode(zkClient, servicePath);
    }

    @Override
    public InetSocketAddress lookupService(RPCRequest rpcRequest) {
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> childrenNodes = CuratorUtils.getChildrenNodes(zkClient, rpcRequest.getRPCServiceName());
        if (null == childrenNodes || childrenNodes.isEmpty()) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcRequest.getRPCServiceName());
        }
        //dubbo://192.168.124.4:20880/com.alibaba.dubbo.demo.DemoService?
        // anyhost=true&application=demo-provider&dubbo=2.0.0&generic=false&interface=com.alibaba.dubbo.demo.DemoService
        // &loadbalance=roundrobin&methods=sayHello&owner=william&pid=56513&side=provider&timestamp=1628408759765
//        URL url = URL.valueOf("");
        rpcRequest.getCustomConfig().put("loadBalance", "random");
        log.info("all provider address:{}", Arrays.toString(childrenNodes.toArray()));
        String targetServiceAddress = loadBalance.whichOne(childrenNodes, rpcRequest);
        log.info("lookup service success.serviceName:{},server:{}", rpcRequest.getRPCServiceName(), targetServiceAddress);
        String[] socketAddressArray = targetServiceAddress.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
