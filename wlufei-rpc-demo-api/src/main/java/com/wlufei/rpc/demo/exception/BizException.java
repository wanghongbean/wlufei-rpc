package com.wlufei.rpc.demo.exception;


/**
 * 业务异常
 *
 * @author labu
 * @date 2021/07/29
 */
public class BizException extends RuntimeException {
    public BizException(String message, Throwable cause) {
        super(message, cause);
    }

    public BizException(String message) {
        super(message);
    }
}
