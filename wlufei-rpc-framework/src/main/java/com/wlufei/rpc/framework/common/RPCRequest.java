package com.wlufei.rpc.framework.common;

import lombok.*;

import java.io.Serializable;


/**
 * rpc request参数
 *
 * @author labu
 * @date 2021/07/29
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class RPCRequest implements Serializable {
    private String requestId;
    private String targetService;
    private String targetMethod;
    private Object[] params;
    private Class<?>[] paramTypes;
    private String group;
    private String version;

    public String getRPCServiceName() {
        return this.getTargetService() + this.getGroup() + this.getVersion();
    }
}
