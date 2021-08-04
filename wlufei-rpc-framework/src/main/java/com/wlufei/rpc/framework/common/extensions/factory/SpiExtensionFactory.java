package com.wlufei.rpc.framework.common.extensions.factory;

import com.wlufei.rpc.framework.common.annotation.SPI;
import com.wlufei.rpc.framework.common.extensions.ExtensionLoader;
import com.wlufei.rpc.framework.common.factory.ExtensionFactory;


/**
 * spi 扩展工厂
 *
 * @author labu
 * @date 2021/08/04
 */
public class SpiExtensionFactory implements ExtensionFactory {

    @Override
    public <T> T getExtension(Class<T> clazz, String name) {
        if (clazz.isInterface() && clazz.isAnnotationPresent(SPI.class)) {
            ExtensionLoader<T> loader = ExtensionLoader.getExtensionLoader(clazz);
            if (loader.getSupportedExtensions().size() > 0) {
                return loader.getAdaptiveExtension();
            }
        }
        return null;
    }
}
