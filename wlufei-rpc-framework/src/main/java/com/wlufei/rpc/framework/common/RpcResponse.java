package com.wlufei.rpc.framework.common;

import lombok.*;

import java.io.Serializable;


/**
 * rpc响应模型
 *
 * @author labu
 * @date 2021/07/29
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Setter
@Getter
@Builder
public class RpcResponse<T> implements Serializable {
    public static final String SUCCESS_CODE = "00";
    public static final String FAIL_CODE = "01";
    public static final String SUCCESS_MESSAGE = "请求成功";
    private String requestId;
    private String code;
    private T data;
    private String message;
    private RuntimeException exception;


    public static <T> RpcResponse<T> onSuccess(T data, String requestId) {
        RpcResponse<T> rpcResponse = new RpcResponse<>();
        rpcResponse.setRequestId(requestId);
        rpcResponse.setCode(SUCCESS_CODE);
        rpcResponse.setMessage(SUCCESS_MESSAGE);
        rpcResponse.setData(data);
        return rpcResponse;
    }

    public static <T> RpcResponse<T> onFail(RuntimeException e, String requestId) {
        RpcResponse<T> rpcResponse = new RpcResponse<>();
        rpcResponse.setRequestId(requestId);
        rpcResponse.setCode(FAIL_CODE);
        rpcResponse.setMessage(e.getMessage());
        rpcResponse.setException(e);
        return rpcResponse;
    }
}
