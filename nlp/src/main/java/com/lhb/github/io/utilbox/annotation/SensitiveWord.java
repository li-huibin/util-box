package com.lhb.github.io.utilbox.annotation;

import java.lang.annotation.*;

/**
 * 敏感词注解，和{@link Sensitive}注解配合使用
 * ignoreApis属性可以用来添加忽略敏感词处理的接口路径
 *
 * @author lihuibin
 **/
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SensitiveWord {
    String[] ignoreApis() default {""};
}
