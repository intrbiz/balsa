package com.intrbiz.balsa.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.balsa.engine.route.impl.exec.DataAdapterWrapperBuilder;
import com.intrbiz.metadata.IsRouteWrapper;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@IsRouteWrapper(DataAdapterWrapperBuilder.class)
public @interface WithDataAdapters
{
    WithDataAdapter[] value();
}
