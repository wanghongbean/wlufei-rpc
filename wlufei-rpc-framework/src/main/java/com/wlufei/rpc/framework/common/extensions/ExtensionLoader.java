package com.wlufei.rpc.framework.common.extensions;


import com.wlufei.rpc.framework.common.RPCRequest;
import com.wlufei.rpc.framework.common.annotation.Adaptive;
import com.wlufei.rpc.framework.common.annotation.SPI;
import com.wlufei.rpc.framework.common.factory.ExtensionFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 扩展加载程序
 *
 * @author labu
 * @date 2021/08/01
 */
@Slf4j
@SuppressWarnings("unchecked")
public final class ExtensionLoader<T> {

    private static final ConcurrentHashMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, Object> SPI_INSTANCES = new ConcurrentHashMap<>();
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

    private ExtensionFactory objectFactory;

    /**
     * ClassLoader实例的自适应类 缓存
     * 项目代码中已经指定的 @Adaptive为 自适应实现的Class 类
     */
    private volatile Class<?> cachedAdaptiveClass = null;

    /**
     * 自适应实例缓存
     * 有可能通过字节码实现的实例对象
     */
    private final Holder<Object> cachedAdaptiveInstance = new Holder<>();

    private ExtensionLoader(Class<T> type) {
        this.type = type;
        objectFactory = type == ExtensionFactory.class ? null : ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension();
    }

    public T getAdaptiveExtension() {
        Object adaptiveInstance = cachedAdaptiveInstance.get();
        if (null == adaptiveInstance) {
            synchronized (cachedAdaptiveInstance) {
                adaptiveInstance = cachedAdaptiveInstance.get();
                if (null == adaptiveInstance) {
                    adaptiveInstance = createAdaptiveExtension();
                    cachedAdaptiveInstance.set(adaptiveInstance);
                }
            }
        }
        return (T) adaptiveInstance;
    }

    public T getDefaultExtensionInstance() {
        if (null == this.cachedDefaultName) {
            throw new IllegalStateException("please set the interface [" + type.getName() + "] default impl.");
        }
        Map<String, Class<?>> cachedInstance = cachedClasses.get();

        return getExtension(this.cachedDefaultName);
    }

    @SuppressWarnings("unchecked")
    private T createAdaptiveExtension() {
        try {
            return injectExtension((T) getAdaptiveExtensionClass().newInstance());
        } catch (Exception e) {
            throw new IllegalStateException("Can not create adaptive extenstion [" + type + "], cause: " + e.getMessage(), e);
        }
    }

    private T injectExtension(T newInstance) {
        log.info("going inject instance:{}", newInstance.toString());
        if (null != objectFactory) {
            //setter方法注入
            for (Method method : newInstance.getClass().getMethods()) {
                if (method.getName().startsWith("set") //set 开头的方法
                        && method.getParameterTypes().length == 1 // 参数类型长度为1
                        && Modifier.isPublic(method.getModifiers())) {//public 修饰
                    Class<?> parameterType = method.getParameterTypes()[0];
                    try {
                        //从方法名中取出属性名称
                        String property = method.getName().length() > 3 ? method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4) : "";
                        Object extension = objectFactory.getExtension(parameterType, property);
                        log.info("parameterType:{},property:{},extension:{}", parameterType, property, extension);
                        if (null != extension) {
                            method.invoke(newInstance, extension);
                            log.info("instance:{},injected:{}", newInstance, extension);
                        }
                    } catch (Exception e) {
                        log.error("fail to inject via method " + method.getName()
                                + " of interface " + type.getName() + ": " + e.getMessage(), e);
                    }

                }
            }
        }
        return newInstance;
    }

    private Class<?> getAdaptiveExtensionClass() {
        getExtensionClasses();
        //加载定义目录下的实现时根据@adaptive标注的自适应实现Class
        if (null != cachedAdaptiveClass) {
            return cachedAdaptiveClass;
        }
        //根据invoker的url 生成自适应的实现
        cachedAdaptiveClass = createAdaptiveExtensionClass();
        return cachedAdaptiveClass;
    }

    /**
     * 创建自适应扩展类
     *
     * @return {@link Class<?>}
     */
    private Class<?> createAdaptiveExtensionClass() {
//        throw new UnsupportedOperationException("暂不支持 type: ["+ type.getName() +"] 生成自适应扩展类");
        String code = createAdaptiveExtensionClassCode();
        ClassLoader classLoader = findClassLoader();
        JavassistCompiler compiler = new JavassistCompiler();//使用javassist编译
        return compiler.compile(code, classLoader);
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
//        if (null != cachedDefaultName && cachedDefaultName.length() > 0) {
//            return getExtension(cachedDefaultName);
//        }
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
        try {
            if (null == instance) {
                SPI_INSTANCES.putIfAbsent(clazz, (T) clazz.newInstance());
                instance = (T) SPI_INSTANCES.get(clazz);
            }
            //dubbo ioc setter注入
            injectExtension(instance);
        } catch (Throwable t) {
            throw new IllegalStateException("Extension instance(name: " + name + ", class: "
                    + type + ",clazz :" + clazz.getName() + ")  could not be instantiated: " + t.getMessage(), t);
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
                                if (clazz.isAnnotationPresent(Adaptive.class)) {
                                    if (null == cachedAdaptiveClass) {
                                        cachedAdaptiveClass = clazz;
                                    } else {
                                        throw new IllegalStateException("More than 1 adaptive class found: "
                                                + cachedAdaptiveClass.getClass().getName()
                                                + ", " + clazz.getClass().getName());
                                    }
                                } else {
                                    //fixme 这块先简单实现加载功能
                                    Class<?> c = extensionClasses.get(name);
                                    if (null == c) {
                                        extensionClasses.put(name, clazz);
                                    } else {
                                        throw new IllegalStateException("Duplicate extension " + type.getName() + " name " + name + " on " + c.getName() + " and " + clazz.getName());
                                    }
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

    public Set<String> getSupportedExtensions() {
        Map<String, Class<?>> extensionClasses = getExtensionClasses();
        return Collections.unmodifiableSet(new TreeSet<>(extensionClasses.keySet()));
    }

    private static ClassLoader findClassLoader() {
        return ExtensionLoader.class.getClassLoader();
    }

    private String createAdaptiveExtensionClassCode() {
        StringBuilder codeBuidler = new StringBuilder();
        Method[] methods = type.getMethods();
        boolean hasAdaptiveAnnotation = Arrays.stream(methods).anyMatch(m -> m.isAnnotationPresent(Adaptive.class));
        // 完全没有Adaptive方法，则不需要生成Adaptive类
        if (!hasAdaptiveAnnotation)
            throw new IllegalStateException("No adaptive method on extension " + type.getName() + ", refuse to create the adaptive class!");

        codeBuidler.append("package " + type.getPackage().getName() + ";");
        codeBuidler.append("\nimport " + ExtensionLoader.class.getName() + ";");
        codeBuidler.append("\nimport " + List.class.getName() + ";");
        codeBuidler.append("\nimport " + String.class.getName() + ";");
        codeBuidler.append("\npublic class " + type.getSimpleName() + "$Adaptive" + " implements " + type.getCanonicalName() + " {");

        for (Method method : methods) {
            Class<?> rt = method.getReturnType();
            Class<?>[] pts = method.getParameterTypes();
            Class<?>[] ets = method.getExceptionTypes();

            Adaptive adaptiveAnnotation = method.getAnnotation(Adaptive.class);
            StringBuilder code = new StringBuilder(512);
            if (adaptiveAnnotation == null) {
                code.append("throw new UnsupportedOperationException(\"method ")
                        .append(method.toString()).append(" of interface ")
                        .append(type.getName()).append(" is not adaptive method!\");");
            } else {
                int urlTypeIndex = -1;
                for (int i = 0; i < pts.length; ++i) {
                    if (pts[i].equals(RPCRequest.class)) {
                        urlTypeIndex = i;
                        break;
                    }
                }
                // 有类型为RPCRequest的参数
                if (urlTypeIndex != -1) {
                    // Null Point check
                    String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"request == null\");",
                            urlTypeIndex);
                    code.append(s);

                    s = String.format("\n%s request = arg%d;", RPCRequest.class.getName(), urlTypeIndex);
                    code.append(s);
                }
                // 这里不去支持参数的类型中的嵌套的自定义参数配置
                else {
                    throw new IllegalStateException("fail to create adaptive class for interface " + type.getName()
                            + ": not found url parameter  of method " + method.getName());
//                    throw new IllegalStateException("fail to create adaptive class for interface " + type.getName()
//                            + ": not found url parameter or url attribute in parameters of method " + method.getName());
                }

                String[] value = adaptiveAnnotation.value();
                // 没有设置Key，则使用“扩展点接口名的点分隔 作为Key
                if (value.length == 0) {
                    char[] charArray = type.getSimpleName().toCharArray();
                    StringBuilder sb = new StringBuilder(128);
                    for (int i = 0; i < charArray.length; i++) {
                        if (Character.isUpperCase(charArray[i])) {
                            if (i != 0) {
                                sb.append(".");
                            }
                            sb.append(Character.toLowerCase(charArray[i]));
                        } else {
                            sb.append(charArray[i]);
                        }
                    }
                    value = new String[]{sb.toString()};
                }
                String s = String.format("\nString methodName = arg%d.getTargetMethod();", urlTypeIndex);
                code.append(s);
                s = String.format("\nif (methodName == null) throw new IllegalArgumentException(\"RPCRequest.targetMethod == null\");");
                code.append(s);

//                String defaultExtName = cachedDefaultName;

                String getNameCode = String.format("\nString extName = request.getCustomConfig().get(\"%s\");",value[0]);
                code.append(getNameCode);
                String extNameNullCheck = String.format("\nif(extName == null) " +
                                "throw new IllegalStateException(\"Fail to get extension(%s) name from url(\" + request.toString() + \") use keys(%s)\");",
                        type.getName(), Arrays.toString(value));
                code.append(extNameNullCheck);
                s = String.format("\n%s extension = (%<s)%s.getExtensionLoader(%s.class).getExtension(extName);",
                        type.getName(), ExtensionLoader.class.getSimpleName(), type.getName());
                code.append(s);

                // return statement
                if (!rt.equals(void.class)) {
                    code.append("\nreturn ");
                }

                s = String.format("extension.%s(", method.getName());
                code.append(s);
                for (int i = 0; i < pts.length; i++) {
                    if (i != 0)
                        code.append(", ");
                    code.append("arg").append(i);
                }
                code.append(");");
            }

            codeBuidler.append("\npublic " + rt.getCanonicalName() + " " + method.getName() + "(");
            Type[] types = method.getGenericParameterTypes();
            for (int i = 0; i < types.length; i++) {
                if (i >0){
                    codeBuidler.append(",");
                }
                Type type = types[i];
//                if (type instanceof ParameterizedType){
//                    Type rawType = ((ParameterizedType) type).getRawType();
                String replace = type.getTypeName().replace("java.util.", "");
                String replace1 = replace.replace("java.lang.", "");
                codeBuidler.append(replace1);
//                    codeBuidler.append(type.getTypeName());
                codeBuidler.append(" ");
                codeBuidler.append("arg" + i);
//                }else {
//
//                }
            }

//            for (int i = 0; i < pts.length; i++) {
//                if (i > 0) {
//                    codeBuidler.append(", ");
//                }
//                //这里针对List<String>实现,反射获取String类型.
//                //todo 待实现类型自动判断生成与接口一致的方法参数
//                Class<?> pt = pts[i];
//                codeBuidler.append(pts[i].getCanonicalName()).append("<String>");
//                codeBuidler.append(" ");
//                codeBuidler.append("arg" + i);
//            }
            codeBuidler.append(")");
            if (ets.length > 0) {
                codeBuidler.append(" throws ");
                for (int i = 0; i < ets.length; i++) {
                    if (i > 0) {
                        codeBuidler.append(", ");
                    }
                    codeBuidler.append(ets[i].getCanonicalName());
                }
            }
            codeBuidler.append(" {");
            codeBuidler.append(code.toString());
            codeBuidler.append("\n}");
        }
        codeBuidler.append("\n}");

        log.info(codeBuidler.toString());
        return codeBuidler.toString();
    }

}
