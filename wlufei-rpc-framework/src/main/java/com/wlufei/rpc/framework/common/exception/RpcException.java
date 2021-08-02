package com.wlufei.rpc.framework.common.exception;

import com.wlufei.rpc.framework.common.enums.RpcErrorMessageEnum;

/**
 * @Author labu
 * @Date 2021/7/29
 * @Description
 */
public class RpcException extends RuntimeException {
    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum, String detail) {
        super(rpcErrorMessageEnum.getMessage() + ":" + detail);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum) {
        super(rpcErrorMessageEnum.getMessage());
    }
}
