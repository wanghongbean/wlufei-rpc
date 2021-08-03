package com.wlufei.rpc.framework.provider.impl;

import com.wlufei.rpc.framework.common.extensions.ExtensionLoader;
import com.wlufei.rpc.framework.config.RpcServiceConfig;
import com.wlufei.rpc.framework.provider.ServiceProvider;
import com.wlufei.rpc.framework.registry.RegistryService;
import com.wlufei.rpc.framework.registry.impl.ZKRegistryServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.wlufei.rpc.framework.registry.impl.ZKRegistryServiceImpl.ZK_REGISTRY_SPI;


/**
 * 服务提供者impl
 *
 * @author labu
 * @date 2021/07/28
 */
@Slf4j
public class ServiceProviderImpl implements ServiceProvider {

    private RegistryService registryService;

    public ServiceProviderImpl() {
        //fixme name暂时写死
        registryService = ExtensionLoader.getExtensionLoader(RegistryService.class).getExtension(ZK_REGISTRY_SPI);
    }

    /**
     * 存储服务注册表
     * <serviceName,IP+port_group+version+serviceName>
     */
    public static final Map<String, Object> REGISTRY_SERVICE_MAP = new ConcurrentHashMap<>();

    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        if (null == rpcServiceConfig) {
            throw new IllegalArgumentException("rpcServiceConfig is null");
        }
        if (!REGISTRY_SERVICE_MAP.containsKey(rpcServiceConfig.getRpcServiceName())) {
            REGISTRY_SERVICE_MAP.put(rpcServiceConfig.getRpcServiceName(), rpcServiceConfig.getTargetService());
        }
        log.info("add service success.serviceName:{}", rpcServiceConfig.getRpcServiceName());
    }

    @Override
    public Object getService(String rpcServiceName) {
        Object service = REGISTRY_SERVICE_MAP.get(rpcServiceName);
        if (null == service) {
            throw new RuntimeException("there have no rpc service provider.rpcServiceName " + rpcServiceName);
        }
        return service;
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            this.addService(rpcServiceConfig);
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            InetSocketAddress socketAddress = new InetSocketAddress(hostAddress, 8888);
            registryService.registryService(rpcServiceConfig.getRpcServiceName(), socketAddress);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("publish " + rpcServiceConfig.getServiceName() + " service failed.");
        }
    }
}
