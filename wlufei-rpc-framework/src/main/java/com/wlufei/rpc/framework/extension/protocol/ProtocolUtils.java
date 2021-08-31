package com.wlufei.rpc.framework.extension.protocol;

import com.wlufei.rpc.framework.common.URL;

public class ProtocolUtils {


    private ProtocolUtils() {
    }

    public static String serviceKey(URL url) {
        return null;
//        return serviceKey(url.getPort(), url.getPath(), url.getParameter(VERSION_KEY),
//                url.getParameter(GROUP_KEY));
    }

    public static String serviceKey(int port, String serviceName, String serviceVersion, String serviceGroup) {
//        serviceGroup = serviceGroup == null ? "" : serviceGroup;
//        GroupServiceKeyCache groupServiceKeyCache = groupServiceKeyCacheMap.get(serviceGroup);
//        if (groupServiceKeyCache == null) {
//            groupServiceKeyCacheMap.putIfAbsent(serviceGroup, new GroupServiceKeyCache(serviceGroup));
//            groupServiceKeyCache = groupServiceKeyCacheMap.get(serviceGroup);
//        }
//        return groupServiceKeyCache.getServiceKey(serviceName, serviceVersion, port);
        return null;
    }


}
