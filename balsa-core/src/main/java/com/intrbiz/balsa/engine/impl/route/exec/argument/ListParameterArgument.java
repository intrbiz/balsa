package com.intrbiz.balsa.engine.impl.route.exec.argument;

import java.lang.annotation.Annotation;
import java.util.List;

import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;
import com.intrbiz.metadata.ListParam;

public final class ListParameterArgument extends ArgumentBuilder<ListParameterArgument>
{
    protected String name;
    
    protected String variable;
    
    public ListParameterArgument()
    {
        super();
    }
    
    @Override
    public String getVariable()
    {
        return this.variable;
    }
    
    public ListParameterArgument name(String name)
    {
        this.name = name;
        return this;
    }
    
    @Override
    public void compile(ExecutorClass cls)
    {
        // allocate the variable we are going to use
        cls.addImport(List.class.getCanonicalName());
        this.variable = cls.allocateExecutorVariable("List<String>");
        // write the code
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // bind parameter ").append(this.index).append("\r\n");
        sb.append("    List<String> ").append(this.variable).append(" = ").append("context.listParam(\"").append(this.name).append("\");\r\n");
    }
    
    @Override
    public void fromAnnotation(Annotation a, Annotation[] parameterAnnotations, Class<?> parameterType)
    {
        ListParam p = (ListParam) a;
        this.name(p.value());
    }

    @Override
    public void verify(Class<?> parameterType)
    {
        if (! List.class.isAssignableFrom(parameterType)) throw new IllegalArgumentException("Parameter argument type must be a List<String> for parameter " + this.name + ".");
    }
}
