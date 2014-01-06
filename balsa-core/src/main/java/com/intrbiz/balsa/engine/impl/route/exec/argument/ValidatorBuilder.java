package com.intrbiz.balsa.engine.impl.route.exec.argument;

import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;
import com.intrbiz.validator.ValidationException;
import com.intrbiz.validator.Validator;

public class ValidatorBuilder
{   
    protected final int parameterIndex;
    
    protected final Validator<?> validator;
    
    protected String field;
    
    public ValidatorBuilder(int parameterIndex, Validator<?> validator)
    {
        super();
        this.parameterIndex = parameterIndex;
        this.validator = validator;
    }

    public Validator<?> getValidator()
    {
        return this.validator;
    }
    
    public Class<?> getType()
    {
        return this.validator == null ? null : this.validator.getType();
    }

    public void compile(ExecutorClass cls, String rawVariable)
    {
        cls.addImport(this.getType().getCanonicalName());
        cls.addImport(Validator.class.getCanonicalName());
        cls.addImport(this.validator.getClass().getCanonicalName());
        cls.addImport(ValidationException.class.getCanonicalName());
        // the converter field
        this.field = cls.allocateField(this.validator.getClass().getSimpleName(), "validator");
        // add construction logic
        StringBuilder cl = cls.getConstructorLogic();
        cl.append("    // init validator\r\n");
        cl.append("    this.").append(this.field).append(" = (").append(this.validator.getClass().getSimpleName()).append(") Validator.fromParameter(this.parameterTypes[").append(this.parameterIndex).append("], this.parameterAnnotations[").append(this.parameterIndex).append("]);\r\n");
        // validate
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // validating ").append(rawVariable).append("\r\n");
        // validate
        sb.append("    try {\r\n");
        sb.append("      ").append("this.").append(this.field).append(".validate(").append(rawVariable).append(");\r\n");
        sb.append("    } catch(ValidationException vex) {\r\n");
        sb.append("      context.addValidationError(vex);\r\n");
        sb.append("    }\r\n");
    }
    
    public void verify(Class<?> parameterType)
    {
        if (! this.validator.canValidate(parameterType)) throw new IllegalArgumentException("Parameter argument type must be a " + this.getType().getName() + ".");
    }
}
