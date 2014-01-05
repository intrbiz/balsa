package com.intrbiz.balsa.engine.impl.route.exec.argument;

import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;
import com.intrbiz.converter.ConversionException;
import com.intrbiz.converter.Converter;

public class ConverterBuilder
{   
    protected final int parameterIndex;
    
    protected final Converter<?> converter;
    
    protected String field;
    
    protected String variable;
    
    public ConverterBuilder(int parameterIndex, Converter<?> converter)
    {
        super();
        this.parameterIndex = parameterIndex;
        this.converter = converter;
    }

    public Converter<?> getConverter()
    {
        return this.converter;
    }

    public String getVariable()
    {
        return this.variable;
    }
    
    public Class<?> getFromType()
    {
        return String.class;
    }
    
    public Class<?> getToType()
    {
        return this.converter == null ? null : this.converter.getType();
    }

    public void compile(ExecutorClass cls, String rawVariable)
    {
        cls.addImport(this.getToType().getCanonicalName());
        cls.addImport(Converter.class.getCanonicalName());
        cls.addImport(this.converter.getClass().getCanonicalName());
        cls.addImport(ConversionException.class.getCanonicalName());
        // the converter field
        this.field = cls.allocateField(this.converter.getClass().getSimpleName(), "converter");
        // add construction logic
        StringBuilder cl = cls.getConstructorLogic();
        cl.append("    // init converter\r\n");
        cl.append("    this.").append(this.field).append(" = (").append(this.converter.getClass().getSimpleName()).append(") Converter.fromParameter(this.parameterTypes[").append(this.parameterIndex).append("], this.parameterAnnotations[").append(this.parameterIndex).append("]);\r\n");
        // allocate our variable        
        this.variable = cls.allocateExecutorVariable(this.getToType().getSimpleName());
        // convert
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // converting ").append(rawVariable).append(" from ").append(this.getFromType().getSimpleName()).append(" to ").append(this.getToType().getSimpleName()).append("\r\n");
        // define the variable
        sb.append("    ").append(this.getToType().getSimpleName()).append(" ").append(this.variable).append(" = null;\r\n");
        // try to convert
        sb.append("    try {\r\n");
        sb.append("      ").append(this.variable).append(" = this.").append(this.field).append(".parseValue(").append(rawVariable).append(");\r\n");
        sb.append("    } catch(ConversionException cex) {\r\n");
        sb.append("      context.addConversionError(cex);\r\n");
        sb.append("    }\r\n");
    }
    
    public void verify(Class<?> parameterType)
    {
        if (! this.converter.canConvertTo(parameterType)) throw new IllegalArgumentException("Parameter argument type must be a " + this.getToType().getName() + ".");
    }
}
