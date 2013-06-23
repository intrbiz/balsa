package com.intrbiz.balsa.engine.impl.route.exec;

import java.lang.annotation.Annotation;

import com.intrbiz.balsa.engine.impl.route.exec.model.ExecutorClass;

public abstract class ArgumentBuilder<T>
{
    protected int index;
    
    protected Class<?> parameterType;
    
    public ArgumentBuilder()
    {
        super();
    }
    
    @SuppressWarnings("unchecked")
    public T index(int index)
    {
        this.index = index;
        return (T) this;
    }
    
    @SuppressWarnings("unchecked")
    public T parameterType(Class<?> pt)
    {
        this.parameterType = pt;
        return (T) this;
    }
    
    public abstract void fromAnnotation(Annotation a, Annotation[] parameterAnnotations, Class<?> parameterType);
    
    public abstract void compile(ExecutorClass cls);
    
    public abstract void verify(Class<?> parameterType);
}
