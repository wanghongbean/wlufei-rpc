package com.wlufei.rpc.framework.extension.protocol;

import com.wlufei.rpc.framework.common.URL;
import com.wlufei.rpc.framework.common.exception.RpcException;
import com.wlufei.rpc.framework.extension.DubboExporter;
import com.wlufei.rpc.framework.extension.Exporter;
import com.wlufei.rpc.framework.extension.Invoker;
import lombok.extern.slf4j.Slf4j;

/**
 * dubbo协议 默认
 * 导出服务
 * 检测配置->URL组装->Invoker创建->导出服务
 * 该导出功能负责把Invoker导出服务并启动服务提供者端server
 * 服务引用
 *
 * @author wanghongbean
 * @date 2021/08/31
 */
@Slf4j
public class DubboProtocol extends AbstractProtocol {
    @Override
    public int getDefaultPort() {
        return 28888;
    }

    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        URL url = invoker.getUrl();

        // export service.
        String key = serviceKey(url);
        DubboExporter<T> exporter = new DubboExporter<T>(invoker, key, exporterMap);
        exporterMap.addExportMap(key, exporter);

        //export an stub service for dispatching event
//        Boolean isStubSupportEvent = url.getParameter(STUB_EVENT_KEY, DEFAULT_STUB_EVENT);
//        Boolean isCallbackservice = url.getParameter(IS_CALLBACK_SERVICE, false);
//        if (isStubSupportEvent && !isCallbackservice) {
//            String stubServiceMethods = url.getParameter(STUB_EVENT_METHODS_KEY);
//            if (stubServiceMethods == null || stubServiceMethods.length() == 0) {
//                if (logger.isWarnEnabled()) {
//                    logger.warn(new IllegalStateException("consumer [" + url.getParameter(INTERFACE_KEY) +
//                            "], has set stubproxy support event ,but no stub methods founded."));
//                }
//
//            }
//        }

        openServer(url);
        optimizeSerialization(url);

        return exporter;
    }

    private void optimizeSerialization(URL url) {

    }

    private void openServer(URL url) {

    }

    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        return null;
    }
}
