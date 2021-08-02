package com.wlufei.rpc.framework.server.impl;

import com.wlufei.rpc.framework.common.RPCRequest;
import com.wlufei.rpc.framework.common.RpcResponse;
import com.wlufei.rpc.framework.server.handler.RPCRequestHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@Slf4j
public class SocketRPCRequestRunnable implements Runnable {

    private Socket socket;
    private RPCRequestHandler rpcRequestHandler;

    public SocketRPCRequestRunnable(Socket socket) {
        this.socket = socket;
        this.rpcRequestHandler = new RPCRequestHandler();

    }

    @Override
    public void run() {
        log.info("accept request. {}", Thread.currentThread().getName());
        ObjectOutputStream objectOutputStream = null;
        RPCRequest rpcRequest = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            rpcRequest = (RPCRequest) objectInputStream.readObject();
            Object result = rpcRequestHandler.handle(rpcRequest);
            objectOutputStream.writeObject(RpcResponse.onSuccess(result, rpcRequest.getRequestId()));
            objectOutputStream.flush();
        } catch (IOException e) {
            log.error("io exception,handle failed.", e);
        } catch (ClassNotFoundException e) {
            log.error("inputStream read failed.", e);
        } catch (RuntimeException e) {
            try {
                if (null != objectOutputStream) {
                    objectOutputStream.writeObject(RpcResponse.onFail(e, rpcRequest.getRequestId()));
                }
            } catch (IOException ioException) {
                log.error("result failed return fail.", e);
            }
            log.error(e.getMessage(), e);
        }

    }
}
