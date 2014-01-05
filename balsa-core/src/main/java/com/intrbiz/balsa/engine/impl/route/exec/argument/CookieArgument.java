package com.intrbiz.balsa.engine.impl.route.exec.argument;

import java.lang.annotation.Annotation;

import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;
import com.intrbiz.metadata.Cookie;

public final class CookieArgument extends ArgumentBuilder<CookieArgument>
{
    protected String name;
    
    protected String variable;
    
    public CookieArgument()
    {
        super();
    }
    
    public CookieArgument name(String name)
    {
        this.name = name;
        return this;
    }
    
    @Override
    public String getVariable()
    {
        return this.variable;
    }
    
    @Override
    public void compile(ExecutorClass cls)
    {
        // allocate the variable we are going to use
        this.variable = cls.allocateExecutorVariable("String");
        // write the code
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // bind parameter ").append(this.index).append("\r\n");
        sb.append("    String ").append(this.variable).append(" = ").append("context.cookie(\"").append(this.name).append("\");\r\n");
    }
    
    @Override
    public void fromAnnotation(Annotation a, Annotation[] parameterAnnotations, Class<?> parameterType)
    {
        Cookie c = (Cookie) a;
        this.name(c.value());
    }
    
    @Override
    public void verify(Class<?> parameterType)
    {
        if (String.class != parameterType) throw new IllegalArgumentException("Parameter argument type must be a String.");
    }
}
