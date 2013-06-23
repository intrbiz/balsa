package com.intrbiz.metadata.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.converter.annotation.ConverterType;
import com.intrbiz.converter.converters.ConverterUUID;
import com.intrbiz.validator.annotation.ValidatorType;
import com.intrbiz.validator.validators.ValidatorUUID;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.METHOD })
@ConverterType(converter = ConverterUUID.class)
@ValidatorType(validator=ValidatorUUID.class)
public @interface IsUUID {
    boolean optional() default true;
}
