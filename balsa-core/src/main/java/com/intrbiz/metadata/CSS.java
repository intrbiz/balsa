package com.intrbiz.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.balsa.engine.impl.route.exec.response.TextResponse;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;

/**
 * Write the String return value of the route to the response
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
@IsResponse(TextResponse.class)
public @interface CSS {
    /**
     * The HTTP response status
     * @return
     */
    HTTPStatus status() default HTTPStatus.OK;
}
