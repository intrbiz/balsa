package com.intrbiz.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.balsa.engine.impl.route.predicate.IgnoreMethodsPredicate;

/**
 * Ignore certain request methods
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@IsRoutePredicate(IgnoreMethodsPredicate.Builder.class)
public @interface IgnoreMethods {
    String[] value();
    int order() default 0;
}
