package com.lhb.github.io.utilbox.annotation;

import java.lang.annotation.*;

/**
 * 敏感词注解，用来标注哪个类属于敏感类
 *
 * @author lihuibin
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Sensitive {
}
