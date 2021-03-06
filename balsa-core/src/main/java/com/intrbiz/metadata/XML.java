package com.intrbiz.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.balsa.engine.impl.route.exec.argument.XMLArgument;
import com.intrbiz.balsa.engine.impl.route.exec.response.XMLResponse;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;

/**
 * A route which will use JAXB to decode and encode the request / response
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
@IsResponse(XMLResponse.class)
@IsArgument(XMLArgument.class)
public @interface XML {
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
