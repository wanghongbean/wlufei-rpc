package com.wlufei.rpc.framework.common.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Adaptive {

    /**
     * 从{@link 'URL'}的Key名，对应的Value作为要Adapt成的Extension名。
     *
     * @return {@link String[]}
     */
    String[] value() default "";
}
