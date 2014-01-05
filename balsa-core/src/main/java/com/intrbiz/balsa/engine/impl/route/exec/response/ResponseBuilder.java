package com.intrbiz.balsa.engine.impl.route.exec.response;

import java.lang.annotation.Annotation;

import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;

public abstract class ResponseBuilder
{   
    public abstract void fromAnnotation(Annotation a, Annotation[] annotations, Class<?> returnType);
    
    public abstract void compile(ExecutorClass cls);
    
    public abstract void verify(Class<?> returnType);
}
