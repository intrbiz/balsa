package com.intrbiz.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.balsa.engine.impl.route.exec.argument.JSONArgument;
import com.intrbiz.balsa.engine.impl.route.exec.response.JSONResponse;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@IsResponse(JSONResponse.class)
@IsArgument(JSONArgument.class)
public @interface JSON
{
    Class<?>[] value() default {};
    
    /**
     * The HTTP response status
     * @return
     */
    HTTPStatus status() default HTTPStatus.OK;
    
    /**
     * If the method returns null, set the HTTP response satus to 404 not found
     * @return
     */
    boolean notFoundIfNull() default false;
}
