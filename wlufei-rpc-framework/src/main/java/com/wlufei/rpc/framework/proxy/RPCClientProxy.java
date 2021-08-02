package com.wlufei.rpc.framework.proxy;

import com.wlufei.rpc.framework.common.enums.RpcErrorMessageEnum;
import com.wlufei.rpc.framework.common.exception.RpcException;
import com.wlufei.rpc.framework.config.RpcServiceConfig;
import com.wlufei.rpc.framework.remoting.RPCRequestTransport;
import com.wlufei.rpc.framework.common.RPCRequest;
import com.wlufei.rpc.framework.common.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.UUID;


/**
 * rpc client 代理
 *
 * @author labu
 * @date 2021/07/29
 */
@Slf4j
public class RPCClientProxy implements InvocationHandler {
    private final RPCRequestTransport rpcRequestTransport;
    private final RpcServiceConfig rpcServiceConfig;

    public RPCClientProxy(RPCRequestTransport rpcRequestTransport, RpcServiceConfig rpcServiceConfig) {
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = rpcServiceConfig;
    }

    public RPCClientProxy(RPCRequestTransport rpcRequestTransport) {
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = new RpcServiceConfig();
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxyInstance(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RPCRequest request = RPCRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .params(args)
                .paramTypes(method.getParameterTypes())
                .targetMethod(method.getName())
                .targetService(method.getDeclaringClass().getName())
                .build();
        RpcResponse<Object> result = (RpcResponse<Object>) rpcRequestTransport.sendRPCRequest(request);
        if (null == result || !Objects.equals(result.getCode(), RpcResponse.SUCCESS_CODE)) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, request.toString());
        }
        return result.getData();
    }
}
