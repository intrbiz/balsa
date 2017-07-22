package com.intrbiz.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.balsa.engine.impl.route.predicate.IgnoreCatchPredicate;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;

/**
 * Ignore certain request paths
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@IsRoutePredicate(IgnoreCatchPredicate.Builder.class)
public @interface IgnoreCatch {
    Class<? extends Throwable>[] value() default {};
    HTTPStatus[] httpErrors() default {};
    int order() default 0;
}
