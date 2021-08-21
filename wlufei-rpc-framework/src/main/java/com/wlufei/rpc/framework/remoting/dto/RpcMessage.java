package com.wlufei.rpc.framework.remoting.dto;

import lombok.*;


/**
 * rpc消息
 *
 * @author wanghongbean
 * @date 2021/08/21
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {
    /**
     * rpc消息类型
     */
    private byte messageType;
    /**
     * 序列化类型
     */
    private byte codec;
    /**
     * 压缩
     */
    private byte compress;
    /**
     * 请求id
     */
    private int requestId;
    /**
     * 数据
     */
    private Object data;
}
