package com.wlufei.rpc.framework.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyRPCServerHandler extends ChannelInboundHandlerAdapter {
    private final RPCRequestHandler rpcRequestHandler;

    public NettyRPCServerHandler() {
        this.rpcRequestHandler = new RPCRequestHandler();
    }

    public void channelRead(ChannelHandlerContext ctx,Object msg){

    }
}
