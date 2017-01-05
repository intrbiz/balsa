package com.intrbiz.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.balsa.engine.impl.route.exec.security.AuthenticatingBuilder;

/**
 * Require that a principal is currently in the process of authenticating and that the initial authentication factor has happened
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@IsSecurityCheck(AuthenticatingBuilder.class)
public @interface RequireAuthenticating
{
}
