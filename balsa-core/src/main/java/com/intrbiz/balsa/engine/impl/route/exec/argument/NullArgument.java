package com.intrbiz.balsa.engine.impl.route.exec.argument;

import java.lang.annotation.Annotation;

import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;

public final class NullArgument extends ArgumentBuilder<NullArgument>
{   
    protected String variable;
    
    public NullArgument()
    {
        super();
    }
    
    public String getVariable()
    {
        return this.variable;
    }
    
    @Override
    public void compile(ExecutorClass cls)
    {
        cls.addImport(this.parameterType.getCanonicalName());
        // allocate the variable we are going to use
        this.variable = cls.allocateExecutorVariable(this.parameterType.getSimpleName());
        // write the code
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // bind parameter ").append(this.index).append("\r\n");
        sb.append("    ").append(this.parameterType.getSimpleName()).append(" ").append(this.variable).append(" = null;\r\n");
    }

    @Override
    public void verify(Class<?> parameterType)
    { 
    }
    
    @Override
    public void fromAnnotation(Annotation a, Annotation[] parameterAnnotations, Class<?> parameterType)
    {
    }
}
