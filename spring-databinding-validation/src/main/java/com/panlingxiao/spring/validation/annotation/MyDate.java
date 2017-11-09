package com.panlingxiao.spring.validation.annotation;

/**
 * Created by panlingxiao on 2016/6/5.
 */
public @interface MyDate {
    String pattern() default "yyyy-MM-dd hh:mm:ss";
}
