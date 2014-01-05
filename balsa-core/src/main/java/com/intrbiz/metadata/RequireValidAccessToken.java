package com.intrbiz.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.balsa.engine.impl.route.exec.security.ValidAccessTokenBuilder;

/**
 * Verify the request token before executing
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@IsSecurityCheck(ValidAccessTokenBuilder.class)
public @interface RequireValidAccessToken {
    Param value() default @Param("key");
}
