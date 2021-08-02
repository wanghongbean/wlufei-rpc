package com.wlufei.rpc.framework.common.extensions;


import lombok.Getter;
import lombok.Setter;

/**
 * 持有人,封装 spi 实例 保证可见性
 *
 * @author labu
 * @date 2021/08/01
 */
//@Getter
//@Setter
public class Holder<T> {
    private volatile T value;

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }
}
