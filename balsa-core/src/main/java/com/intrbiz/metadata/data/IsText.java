package com.intrbiz.metadata.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.converter.annotation.ConverterType;
import com.intrbiz.converter.converters.ConverterString;
import com.intrbiz.validator.annotation.ValidatorType;
import com.intrbiz.validator.validators.ValidatorText;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.METHOD })
@ConverterType(converter = ConverterString.class)
@ValidatorType(validator=ValidatorText.class)
public @interface IsText {
    boolean optional() default true;
}
