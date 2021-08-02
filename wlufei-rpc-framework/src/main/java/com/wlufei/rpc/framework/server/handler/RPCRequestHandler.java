package com.wlufei.rpc.framework.server.handler;


import com.wlufei.rpc.framework.common.RPCRequest;
import com.wlufei.rpc.framework.common.exception.RpcException;
import com.wlufei.rpc.framework.provider.ServiceProvider;
import com.wlufei.rpc.framework.provider.impl.ServiceProviderImpl;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * rpc request处理程序
 *
 * @author labu
 * @date 2021/07/29
 */
@Slf4j
public class RPCRequestHandler {
    private ServiceProvider serviceProvider;

    public RPCRequestHandler() {
        this.serviceProvider = new ServiceProviderImpl();
    }

    public Object handle(RPCRequest rpcRequest) {
        log.info("handle rpc request. rpcRequest:{}", rpcRequest.toString());
        Object service = serviceProvider.getService(rpcRequest.getRPCServiceName());
        return handleMethodInvoke(service, rpcRequest);
    }

    private Object handleMethodInvoke(Object service, RPCRequest rpcRequest) {
        try {
            Method method = service.getClass().getMethod(rpcRequest.getTargetMethod(), rpcRequest.getParamTypes());
            Object result = method.invoke(service, rpcRequest.getParams());
            log.info("rpc success.requestId:{},serviceName:{},methodName:{},result:{}", rpcRequest.getRequestId(),
                    rpcRequest.getRPCServiceName(), rpcRequest.getTargetMethod(), result);
            return result;
//        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//            log.error(e.getMessage(), e);
//            throw new RpcException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RpcException(e.getMessage(), e);
        }
    }
}
