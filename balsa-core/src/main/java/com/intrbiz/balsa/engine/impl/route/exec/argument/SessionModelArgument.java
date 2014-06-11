package com.intrbiz.balsa.engine.impl.route.exec.argument;

import java.lang.annotation.Annotation;

import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;
import com.intrbiz.metadata.SessionModel;

public final class SessionModelArgument extends ArgumentBuilder<SessionModelArgument>
{
    protected String name;
    
    protected Class<?> type;
    
    protected String variable;
    
    public SessionModelArgument()
    {
        super();
    }
    
    @Override
    public String getVariable()
    {
        return this.variable;
    }
    
    public SessionModelArgument name(String name)
    {
        this.name = name;
        return this;
    }
    
    public SessionModelArgument type(Class<?> type)
    {
        this.type = type;
        return this;
    }
    
    @Override
    public void compile(ExecutorClass cls)
    {
        // allocate the variable we are going to use
        cls.addImport(this.type.getCanonicalName());
        this.variable = cls.allocateExecutorVariable(this.type.getSimpleName());
        // write the code
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // bind parameter ").append(this.index).append("\r\n");
        sb.append("    ").append(this.type.getSimpleName()).append(" ").append(this.variable).append(" = ").append("context.sessionModel(\"").append(this.name).append("\");\r\n");
    }
    
    @Override
    public void fromAnnotation(Annotation a, Annotation[] parameterAnnotations, Class<?> parameterType)
    {
        SessionModel v = (SessionModel) a;
        this.name(v.value());
        this.type(parameterType);
    }

    @Override
    public void verify(Class<?> parameterType)
    {
    }
}
