package com.wlufei.rpc.framework.client;

import com.wlufei.rpc.framework.common.enums.RpcErrorMessageEnum;
import com.wlufei.rpc.framework.common.exception.RpcException;
import com.wlufei.rpc.framework.common.extensions.ExtensionLoader;
import com.wlufei.rpc.framework.registry.RegistryService;
import com.wlufei.rpc.framework.registry.ServiceDiscovery;
import com.wlufei.rpc.framework.registry.impl.ZKServiceDiscoveryImpl;
import com.wlufei.rpc.framework.remoting.RPCRequestTransport;
import com.wlufei.rpc.framework.common.RPCRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import static com.wlufei.rpc.framework.registry.impl.ZKRegistryServiceImpl.ZK_REGISTRY_SPI;


/**
 * 套接字 rpc request 传输
 *
 * @author labu
 * @date 2021/07/29
 */
@Slf4j
public class SocketRPCClient implements RPCRequestTransport {
    private final RegistryService registryService;

    public SocketRPCClient() {
        registryService = ExtensionLoader.getExtensionLoader(RegistryService.class).getExtension(ZK_REGISTRY_SPI);
    }

    @Override
    public Object sendRPCRequest(RPCRequest rpcRequest) {
        InetSocketAddress inetSocketAddress = registryService.lookupService(rpcRequest);
        log.info("get service provider inetAddr:{}",inetSocketAddress.toString());
        Socket socket = new Socket();
        try {
            socket.connect(inetSocketAddress);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(rpcRequest);
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new RpcException(RpcErrorMessageEnum.CLIENT_CONNECT_SERVER_FAILURE);
        }
    }
}
