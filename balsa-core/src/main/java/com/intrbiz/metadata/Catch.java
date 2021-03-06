package com.intrbiz.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.balsa.http.HTTP.HTTPStatus;

/**
 * Define that this method is an error route, valid for the given exceptions
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(Catches.class)
public @interface Catch {
    Class<? extends Throwable>[] value() default { Throwable.class };
    HTTPStatus[] httpErrors() default {};
}
