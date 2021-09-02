package com.wlufei.rpc.framework.extension;

public class DubboExporter<T> implements Exporter {
    private Invoker invoker;
    private String key;
    private DelegateExporterMap map;

    public DubboExporter(Invoker invoker, String key, DelegateExporterMap map) {
        this.invoker = invoker;
        this.key = key;
        this.map = map;
    }
}
