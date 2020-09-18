package com.xyoye.mmkv_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by xyoye on 2020/9/10.
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface MMKVJavaClass {
    String className() default "";

    boolean initMMKV() default false;
}
