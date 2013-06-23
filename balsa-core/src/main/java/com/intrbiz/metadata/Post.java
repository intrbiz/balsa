package com.intrbiz.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.balsa.engine.impl.route.Route.PostRouteBuilder;

/**
 * Define that this method can handle a POST request of the given pattern
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@IsRoute(PostRouteBuilder.class)
public @interface Post {
    String value();
    boolean regex() default false;
    String[] as() default {};
}
