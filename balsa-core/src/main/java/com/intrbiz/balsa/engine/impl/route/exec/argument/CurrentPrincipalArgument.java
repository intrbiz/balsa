package com.intrbiz.balsa.engine.impl.route.exec.argument;

import java.lang.annotation.Annotation;

import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;

public final class CurrentPrincipalArgument extends ArgumentBuilder<CurrentPrincipalArgument>
{    
    protected Class<?> type;
    
    protected String variable;
    
    public CurrentPrincipalArgument()
    {
        super();
    }
    
    @Override
    public String getVariable()
    {
        return this.variable;
    }
    
    public CurrentPrincipalArgument name()
    {
        return this;
    }
    
    public CurrentPrincipalArgument type(Class<?> type)
    {
        this.type = type;
        return this;
    }
    
    @Override
    public void compile(ExecutorClass cls)
    {
        // allocate the variable we are going to use
        cls.addImport(this.type.getCanonicalName());
        this.variable = cls.allocateExecutorVariable(this.type.getSimpleName(), "current_principal");
        // write the code
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // bind parameter ").append(this.index).append("\r\n");
        sb.append("    ").append(this.type.getSimpleName()).append(" ").append(this.variable).append(" = ").append("context.currentPrincipal();\r\n");
    }
    
    @Override
    public void fromAnnotation(Annotation a, Annotation[] parameterAnnotations, Class<?> parameterType)
    {
        this.type(parameterType);
    }

    @Override
    public void verify(Class<?> parameterType)
    {
    }
}
