package com.wlufei.rpc.framework.extension;

import com.wlufei.rpc.framework.common.URL;

/**
 * 调用程序
 *
 * @author wanghongbean
 * @date 2021/08/26
 */
public class Invoker<T> {
    private URL url;


    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }
}
