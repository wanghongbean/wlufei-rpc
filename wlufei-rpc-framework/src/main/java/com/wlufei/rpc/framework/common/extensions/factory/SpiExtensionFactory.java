package com.wlufei.rpc.framework.common.extensions.factory;

import com.wlufei.rpc.framework.common.annotation.SPI;
import com.wlufei.rpc.framework.common.extensions.ExtensionLoader;
import com.wlufei.rpc.framework.common.factory.ExtensionFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;


/**
 * spi 扩展工厂
 *
 * @author labu
 * @date 2021/08/04
 */
@Slf4j
public class SpiExtensionFactory implements ExtensionFactory {
    public static final String SPI_FACTORY = "spiFactory";

    @Override
    public <T> T getExtension(Class<T> clazz, String name) {
        if (clazz.isInterface() && clazz.isAnnotationPresent(SPI.class)) {
            ExtensionLoader<T> loader = ExtensionLoader.getExtensionLoader(clazz);
            log.info("loader:{} supported extensions:{}", loader.getClass().getName(), Arrays.toString(loader.getSupportedExtensions().toArray()));
            if (loader.getSupportedExtensions().size() > 0) {
                return loader.getAdaptiveExtension();
//                T instance;
//                try {
//                    instance = loader.getAdaptiveExtension();
//                } catch (Exception e) {
//                    log.error("load adaptiveExtension error. cause "+e.getMessage(),e);
//                    instance = loader.getDefaultExtensionInstance();
//                }
//                return instance;
            }
        }
        return null;
    }
}
