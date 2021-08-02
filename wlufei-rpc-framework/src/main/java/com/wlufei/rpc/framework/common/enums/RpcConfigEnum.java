package com.wlufei.rpc.framework.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * rpc配置枚举
 *
 * @author labu
 * @date 2021/07/28
 */
@AllArgsConstructor
@Getter
public enum RpcConfigEnum {
    RPC_CONFIG_PATH("rpc.properties"),
    ZK_ADDRESS("rpc.zookeeper.address");

    private final String propertyValue;

}
