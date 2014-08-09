package com.intrbiz.balsa.engine.impl.route.exec.argument;

import java.lang.annotation.Annotation;

import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;
import com.intrbiz.metadata.SessionVar;

public final class SessionVarArgument extends ArgumentBuilder<SessionVarArgument>
{
    protected String name;
    
    protected Class<?> type;
    
    protected String variable;
    
    public SessionVarArgument()
    {
        super();
    }
    
    @Override
    public String getVariable()
    {
        return this.variable;
    }
    
    public SessionVarArgument name(String name)
    {
        this.name = name;
        return this;
    }
    
    public SessionVarArgument type(Class<?> type)
    {
        this.type = type;
        return this;
    }
    
    @Override
    public void compile(ExecutorClass cls)
    {
        // allocate the variable we are going to use
        cls.addImport(this.type.getCanonicalName());
        this.variable = cls.allocateExecutorVariable(this.type.getSimpleName(), "session_var");
        // write the code
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // bind parameter ").append(this.index).append("\r\n");
        sb.append("    ").append(this.type.getSimpleName()).append(" ").append(this.variable).append(" = ").append("context.sessionVar(\"").append(this.name).append("\");\r\n");
    }
    
    @Override
    public void fromAnnotation(Annotation a, Annotation[] parameterAnnotations, Class<?> parameterType)
    {
        SessionVar v = (SessionVar) a;
        this.name(v.value());
        this.type(parameterType);
    }

    @Override
    public void verify(Class<?> parameterType)
    {
    }
}
