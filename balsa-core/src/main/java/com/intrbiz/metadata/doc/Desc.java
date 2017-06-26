package com.intrbiz.metadata.doc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description of something of documentation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.CONSTRUCTOR })
public @interface Desc {
    String[] value();
}
