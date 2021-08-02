package com.wlufei.rpc.framework.common.extensions;


import com.wlufei.rpc.framework.common.annotation.SPI;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 扩展加载程序
 *
 * @author labu
 * @date 2021/08/01
 */
@Slf4j
public final class ExtensionLoader<T> {

    private static final String SPI_CONFIG_DIR = "META-INF/dubbo/";

    private static final ConcurrentHashMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, Object> SPI_INSTANCES = new ConcurrentHashMap<>();
    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");

    /**
     * JDK SPI默认的spi实现的目录
     */
    private static final String SERVICES_DIRECTORY = "META-INF/services/";

    /**
     * dubbo应用定义的spi实现的目录
     */
    private static final String DUBBO_DIRECTORY = "META-INF/dubbo/";

    /**
     * dubbo服务内部spi实现的目录
     */
    private static final String DUBBO_INTERNAL_DIRECTORY = DUBBO_DIRECTORY + "internal/";

    // ==================================

    /**
     * spi的类型
     */
    private final Class<?> type;
    /**
     * 缓存实例
     */
    private final ConcurrentHashMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();
    /**
     * 缓存的类 name对应的Class类型
     * <name,Class>
     */
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    /**
     * 记录读取文件过程中的异常
     */
    private final ConcurrentHashMap<String, IllegalStateException> exceptions = new ConcurrentHashMap<>();
    /**
     * 缓存的默认spi实现名称
     */
    private String cachedDefaultName;

    private ExtensionLoader(Class<T> type) {
        this.type = type;
    }

    private static <T> boolean withExtensionAnnotation(Class<T> type) {
        return type.isAnnotationPresent(SPI.class);
    }

    /**
     * 得到扩展加载程序
     *
     * @param type 类型
     * @return {@link ExtensionLoader<T>}
     */
    @SuppressWarnings("unchecked")
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (null == type) {
            throw new IllegalArgumentException("extension type == null");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("extension type(" + type + ") is not interface!");
        }
        if (!withExtensionAnnotation(type)) {
            throw new IllegalArgumentException("extension type(" + type + ") is not extension,because WITHOUT @"
                    + SPI.class.getSimpleName() + " annotation!");
        }

        //从缓存中查找
        ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (null == loader) {
            //实例化,并放入缓存
            EXTENSION_LOADERS.put(type, new ExtensionLoader<>(type));
            loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return loader;
    }

    /**
     * 加载spi实现
     *
     * @param name spi实现的name
     * @return {@link T}
     */
    @SuppressWarnings("unchecked")
    public T getExtension(String name) {
        if (null == name || name.length() == 0) {
            throw new IllegalArgumentException("spi intance name can not be null");
        }
        Holder<Object> holder = cachedInstances.get(name);
        if (null == holder) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        Object instance = holder.get();
        if (null == instance) {
            //double check
            synchronized (holder) {
                instance = holder.get();
                if (null == instance) {
                    instance = createExtensionInstance(name);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    /**
     * 创建扩展实例
     *
     * @param name 的名字
     * @return {@link T}
     */
    @SuppressWarnings("unchecked")
    private T createExtensionInstance(String name) {
        //指定name的 type的类型
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new IllegalStateException("No such extension \"" + name + "\" for " + type.getName() + "!");
        }
        T instance = (T) SPI_INSTANCES.get(clazz);
        if (null == instance) {
            try {
                SPI_INSTANCES.putIfAbsent(clazz, (T) clazz.newInstance());
                instance = (T) SPI_INSTANCES.get(clazz);
            } catch (Throwable t) {
                throw new IllegalStateException("Extension instance(name: " + name + ", class: "
                        + type + ")  could not be instantiated: " + t.getMessage(), t);
            }
        }

        return instance;
    }

    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.get();
        if (null == classes) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (null == classes) {
                    classes = loadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Class<?>> loadExtensionClasses() {
        final SPI annotation = this.type.getAnnotation(SPI.class);
        //加载默认实现
        if (null != annotation) {
            String defaultValue = annotation.value().trim();
            if (defaultValue.length() > 0) {
                String[] names = NAME_SEPARATOR.split(defaultValue);
                if (names.length > 1) {
                    throw new IllegalStateException("more than 1 default extension name on extension " + type.getName()
                            + ": " + Arrays.toString(names));
                }
                if (names.length == 1) {
                    cachedDefaultName = names[0];
                }
            }
        }
        Map<String, Class<?>> extensionClasses = new HashMap<>();
        loadFile(extensionClasses, DUBBO_INTERNAL_DIRECTORY);
        loadFile(extensionClasses, DUBBO_DIRECTORY);
        loadFile(extensionClasses, SERVICES_DIRECTORY);
        return extensionClasses;
    }

    private void loadFile(Map<String, Class<?>> extensionClasses, String dir) {
        String fileName = dir + type.getName();
        ClassLoader classLoader = findClassLoader();
        Enumeration<URL> resources;
        try {
            if (null != classLoader) {
                resources = classLoader.getResources(fileName);
            } else {
                //fixme 暂时没有想到获取不到ClassLoader的场景
                throw new NullPointerException("classloader is null");
            }
            if (null == resources) {
                return;
            }
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                log.info("loaded resource element url is:{}", url);
                loadResources(extensionClasses, classLoader, url);
            }
        } catch (Throwable t) {
            log.error("Exception when load extension class(interface: "
                    + type + ", description file: " + fileName + ").", t);
        }


    }

    private void loadResources(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, URL url) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(url.openStream(), "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            try {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    //去除注释内容
                    final int ci = line.indexOf('#');
                    if (ci >= 0) {
                        line = line.substring(0, ci);
                        line = line.trim();
                    }
                    if (line.length() > 0) {
                        try {
                            int i = line.indexOf("=");
                            String name = null;
                            if (i > 0) {
                                name = line.substring(0, i).trim();
                                line = line.substring(i + 1).trim();
                            }
                            if (line.length() > 0) {
                                //获取大Class类,非实例
                                Class<?> clazz = Class.forName(line, true, classLoader);
                                if (!type.isAssignableFrom(clazz)) {
                                    throw new IllegalArgumentException("Error when load extension class(interface: "
                                            + type + ", class line: " + clazz.getName() + "), class "
                                            + clazz.getName() + "is not subtype of interface.");
                                }
                                //fixme 这块先简单实现加载功能
                                Class<?> c = extensionClasses.get(name);
                                if (null == c) {
                                    extensionClasses.put(name, clazz);
                                } else {
                                    throw new IllegalStateException("Duplicate extension " + type.getName() + " name " + name + " on " + c.getName() + " and " + clazz.getName());
                                }
                            }

                        } catch (Throwable t) {
                            IllegalStateException e = new IllegalStateException("Failed to load extension class(interface: " + type + ", class line: " + line + ") in " + url + ", cause: " + t.getMessage(), t);
                            exceptions.put(line, e);
                        }
                    }
                }
            } finally {
                bufferedReader.close();
            }
        } catch (Throwable t) {
            log.error("Exception when load extension class(interface: "
                    + type + ", class file: " + url + ") in " + url, t);
        }
    }

    private static ClassLoader findClassLoader() {
        return ExtensionLoader.class.getClassLoader();
    }


}
