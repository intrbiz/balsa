package com.intrbiz.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.balsa.engine.impl.route.exec.wrapper.StatusWrapperBuilder;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;

/**
 * Set the default response status for this route
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@IsRouteWrapper(StatusWrapperBuilder.class)
public @interface Status {
    HTTPStatus value() default HTTPStatus.OK;
}
