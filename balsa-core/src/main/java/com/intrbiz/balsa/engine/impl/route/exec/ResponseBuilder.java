package com.intrbiz.balsa.engine.impl.route.exec;

import java.lang.annotation.Annotation;

import com.intrbiz.balsa.engine.impl.route.exec.model.ExecutorClass;

public abstract class ResponseBuilder
{   
    public abstract void fromAnnotation(Annotation a, Annotation[] annotations, Class<?> returnType);
    
    public abstract void compile(ExecutorClass cls);
    
    public abstract void verify(Class<?> returnType);
}
