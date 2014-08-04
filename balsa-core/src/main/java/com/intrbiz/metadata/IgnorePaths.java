package com.intrbiz.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.balsa.engine.impl.route.predicate.IgnorePathsPredicate;

/**
 * Ignore certain request paths
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@IsRoutePredicate(IgnorePathsPredicate.Builder.class)
public @interface IgnorePaths {
    String[] value();
    boolean regex() default false;
    int order() default 0;
}
