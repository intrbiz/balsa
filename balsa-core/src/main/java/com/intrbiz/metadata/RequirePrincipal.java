package com.intrbiz.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.balsa.engine.impl.route.exec.security.ValidPrincipalBuilder;

/**
 * Require a weakly validated principal.
 * 
 * This is a weaker version of @RequireValidPrincipal() which 
 * you should probably be using
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@IsSecurityCheck(ValidPrincipalBuilder.class)
public @interface RequirePrincipal
{
}
