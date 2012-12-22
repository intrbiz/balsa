package com.intrbiz.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.balsa.engine.route.RouteExecutorFactory;

/**
 * Define the executor for a route handler
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Executor {
    Class<? extends RouteExecutorFactory> value();
}
