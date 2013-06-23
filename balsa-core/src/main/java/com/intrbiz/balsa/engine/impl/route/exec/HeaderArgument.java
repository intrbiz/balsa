package com.intrbiz.balsa.engine.impl.route.exec;

import java.lang.annotation.Annotation;

import com.intrbiz.balsa.engine.impl.route.exec.model.ExecutorClass;
import com.intrbiz.metadata.Header;

public final class HeaderArgument extends ArgumentBuilder<HeaderArgument>
{
    protected String name;
    
    public HeaderArgument()
    {
        super();
    }
    
    public HeaderArgument name(String name)
    {
        this.name = name;
        return this;
    }
    
    @Override
    public void compile(ExecutorClass cls)
    {
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // bind parameter ").append(this.index).append("\r\n");
        sb.append("    String p").append(this.index).append(" = ").append("context.header(\"").append(this.name).append("\");\r\n");
    }
    
    @Override
    public void verify(Class<?> parameterType)
    {
        if (String.class != parameterType) throw new IllegalArgumentException("Parameter argument type must be a String."); 
    }
    
    @Override
    public void fromAnnotation(Annotation a, Annotation[] parameterAnnotations, Class<?> parameterType)
    {
        Header h = (Header) a;
        this.name(h.value());
    }
}
