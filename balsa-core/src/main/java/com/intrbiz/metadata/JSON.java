package com.intrbiz.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.balsa.engine.impl.route.exec.JSONArgument;
import com.intrbiz.balsa.engine.impl.route.exec.JSONResponse;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@IsResponse(JSONResponse.class)
@IsArgument(JSONArgument.class)
public @interface JSON
{
}
