package com.wlufei.rpc.framework.extension.protocol;

import com.wlufei.rpc.framework.common.URL;
import com.wlufei.rpc.framework.extension.DelegateExporterMap;
import com.wlufei.rpc.framework.extension.Protocol;
import com.wlufei.rpc.framework.remoting.RemoteConstans;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象的协议
 *
 * @author wanghongbean
 * @date 2021/08/31
 */
@Slf4j
public abstract class AbstractProtocol implements Protocol {

    protected final DelegateExporterMap exporterMap = new DelegateExporterMap();

    protected static String serviceKey(URL url) {
        int port = url.getParameter(RemoteConstans.BIND_PORT_KEY, url.getPort());
        return serviceKey(port, url.getPath(), url.getParameter(RemoteConstans.VERSION_KEY), url.getParameter(RemoteConstans.GROUP_KEY));
    }

    protected static String serviceKey(int port, String serviceName, String serviceVersion, String serviceGroup) {
        return ProtocolUtils.serviceKey(port, serviceName, serviceVersion, serviceGroup);
    }
}
