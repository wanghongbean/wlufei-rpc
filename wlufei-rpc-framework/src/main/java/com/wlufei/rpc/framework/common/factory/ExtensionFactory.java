package com.wlufei.rpc.framework.common.factory;

import com.wlufei.rpc.framework.common.annotation.SPI;

@SPI
public interface ExtensionFactory {


    /**
     * 得到扩展
     *
     * @param clazz clazz
     * @param name  的名字
     * @return {@link T}
     */
    <T> T getExtension(Class<T> clazz, String name);
}
