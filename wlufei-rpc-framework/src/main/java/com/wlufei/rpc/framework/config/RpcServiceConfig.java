package com.wlufei.rpc.framework.config;


import lombok.*;

/**
 * rpc服务配置
 *
 * @author labu
 * @date 2021/07/28
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class RpcServiceConfig {
    /**
     * 版本
     */
    private String version = "1";
    /**
     * 组名
     */
    private String group = "default";
    /**
     * 目标服务
     */
    private Object targetService;

    public String getServiceName() {
        return targetService.getClass().getInterfaces()[0].getCanonicalName();
    }

    public String getRpcServiceName() {
        return getServiceName() + this.getGroup() + this.getVersion();
    }
}
