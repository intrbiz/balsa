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
    public static final int FIRST = -1_000_000;
    public static final int LAST  = 1_000_000;
    int value() default 0;
}
