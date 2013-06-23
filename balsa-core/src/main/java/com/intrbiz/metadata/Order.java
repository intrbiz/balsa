package com.intrbiz.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify the order of routes which conflict
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Order {
    public static final int FIRST = Integer.MIN_VALUE;
    public static final int LAST = Integer.MAX_VALUE;
    int value() default 0;
}
