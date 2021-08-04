package com.wlufei.rpc.framework.common.extensions.factory;

import com.wlufei.rpc.framework.common.annotation.Adaptive;
import com.wlufei.rpc.framework.common.extensions.ExtensionLoader;
import com.wlufei.rpc.framework.common.factory.ExtensionFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * 自适应扩展工厂
 *
 * @author labu
 * @date 2021/08/04
 */
@Adaptive
public class AdaptiveExtensionFactory implements ExtensionFactory {
    private final List<ExtensionFactory> factories;

    public AdaptiveExtensionFactory(){
        ExtensionLoader<ExtensionFactory> loader = ExtensionLoader.getExtensionLoader(ExtensionFactory.class);
        List<ExtensionFactory> list = new ArrayList<>();
        //加载项目配置支持的ExtensionFactory类
        for (String extension : loader.getSupportedExtensions()) {
            ExtensionFactory factory = loader.getExtension(extension);
            list.add(factory);
        }
        factories = Collections.unmodifiableList(list);
    }

    @Override
    public <T> T getExtension(Class<T> clazz, String name) {
        for (ExtensionFactory factory : factories) {
            T extension = factory.getExtension(clazz, name);
            if (null != extension){
                return extension;
            }
        }
        return null;
    }
}
