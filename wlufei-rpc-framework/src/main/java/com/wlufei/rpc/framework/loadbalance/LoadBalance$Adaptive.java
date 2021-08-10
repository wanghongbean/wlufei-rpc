package com.wlufei.rpc.framework.loadbalance;
import com.wlufei.rpc.framework.common.extensions.ExtensionLoader;
import java.util.List;
import java.lang.String;
public class LoadBalance$Adaptive implements com.wlufei.rpc.framework.loadbalance.LoadBalance {
    public java.lang.String whichOne(List<String> arg0,com.wlufei.rpc.framework.common.RPCRequest arg1) {
        if (arg1 == null) throw new IllegalArgumentException("request == null");
        com.wlufei.rpc.framework.common.RPCRequest request = arg1;
        String methodName = arg1.getTargetMethod();
        if (methodName == null) throw new IllegalArgumentException("RPCRequest.targetMethod == null");
        String extName = request.getCustomConfig().get("loadBalance");
        if(extName == null) throw new IllegalStateException("Fail to get extension(com.wlufei.rpc.framework.loadbalance.LoadBalance) name from url(" + request.toString() + ") use keys([loadBalance])");
        com.wlufei.rpc.framework.loadbalance.LoadBalance extension = (com.wlufei.rpc.framework.loadbalance.LoadBalance)ExtensionLoader.getExtensionLoader(com.wlufei.rpc.framework.loadbalance.LoadBalance.class).getExtension(extName);
        return extension.whichOne(arg0, arg1);
    }
}