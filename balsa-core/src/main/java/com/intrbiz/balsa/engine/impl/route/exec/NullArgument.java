package com.intrbiz.balsa.engine.impl.route.exec;

import java.lang.annotation.Annotation;

import com.intrbiz.balsa.engine.impl.route.exec.model.ExecutorClass;

public final class NullArgument extends ArgumentBuilder<NullArgument>
{   
    public NullArgument()
    {
        super();
    }
    
    @Override
    public void compile(ExecutorClass cls)
    {
        cls.addImport(this.parameterType.getCanonicalName());
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // bind parameter ").append(this.index).append("\r\n");
        sb.append("    ").append(this.parameterType.getSimpleName()).append(" p").append(this.index).append(" = null;\r\n");
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
