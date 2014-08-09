package com.intrbiz.balsa.engine.impl.route.exec.argument;

import java.util.List;

import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;
import com.intrbiz.validator.ValidationException;
import com.intrbiz.validator.Validator;

public class ValidatorBuilder
{   
    protected final int parameterIndex;
    
    protected final Validator<?> validator;
    
    protected String field;
    
    protected boolean list;
    
    public ValidatorBuilder(int parameterIndex, Validator<?> validator, boolean list)
    {
        super();
        this.parameterIndex = parameterIndex;
        this.validator = validator;
        this.list = list;
    }

    public Validator<?> getValidator()
    {
        return this.validator;
    }
    
    public Class<?> getType()
    {
        return this.validator == null ? null : this.validator.getType();
    }
    
    public boolean isList()
    {
        return this.list;
    }

    public void compile(ExecutorClass cls, String rawVariable)
    {
        cls.addImport(this.getType().getCanonicalName());
        cls.addImport(Validator.class.getCanonicalName());
        cls.addImport(this.validator.getClass().getCanonicalName());
        cls.addImport(ValidationException.class.getCanonicalName());
        if (this.list) cls.addImport(List.class.getCanonicalName());
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
        sb.append("      ").append(rawVariable).append(" = ").append("this.").append(this.field).append(".validate" + (this.list ? "List" : "") + "(").append(rawVariable).append(");\r\n");
        sb.append("    } catch(ValidationException vex) {\r\n");
        sb.append("      context.addValidationError(vex);\r\n");
        sb.append("    }\r\n");
    }
    
    public void verify(Class<?> parameterType)
    {
        if (! this.validator.canValidate(parameterType)) throw new IllegalArgumentException("Parameter argument type must be a " + this.getType().getName() + ".");
    }
}
