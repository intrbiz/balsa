package com.intrbiz.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.balsa.engine.impl.route.exec.security.PermissionsBuilder;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Repeatable(RequirePermissions.class)
@IsSecurityCheck(PermissionsBuilder.class)
public @interface RequirePermission
{
    String value();
}
