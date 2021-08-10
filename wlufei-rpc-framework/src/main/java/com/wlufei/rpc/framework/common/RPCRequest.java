package com.wlufei.rpc.framework.common;

import lombok.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


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
    //Dubbo中使用URL模型承接调用方自定义的参数,实现在真正调用时确定具体调用的SPI实现类,version1.1简单使用map实现
    private final Map<String, String> customConfig = new HashMap<>();


    public String getRPCServiceName() {
        return this.getTargetService() + this.getGroup() + this.getVersion();
    }
}
