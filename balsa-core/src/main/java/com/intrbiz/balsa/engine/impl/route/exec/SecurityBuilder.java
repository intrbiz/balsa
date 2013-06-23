package com.intrbiz.balsa.engine.impl.route.exec;

import java.lang.annotation.Annotation;

import com.intrbiz.balsa.engine.impl.route.exec.model.ExecutorClass;

public abstract class SecurityBuilder
{   
    public abstract void fromAnnotation(Annotation a);
    
    public abstract void compile(ExecutorClass cls);
}
