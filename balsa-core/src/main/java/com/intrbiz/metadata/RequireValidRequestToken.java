package com.intrbiz.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.balsa.engine.impl.route.exec.ValidRequestBuilder;

/**
 * Verify the request token before executing
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@IsSecurityCheck(ValidRequestBuilder.class)
public @interface RequireValidRequestToken {
    Param value() default @Param("request-token");
}
