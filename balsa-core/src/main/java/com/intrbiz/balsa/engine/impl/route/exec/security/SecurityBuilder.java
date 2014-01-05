package com.intrbiz.balsa.engine.impl.route.exec.security;

import java.lang.annotation.Annotation;

import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;

public abstract class SecurityBuilder
{   
    public abstract void fromAnnotation(Annotation a);
    
    public abstract void compile(ExecutorClass cls);
    
    /**
     * There can only be one check of this type on a route
     * @return
     */
    public boolean isSingular()
    {
        return true;
    }
}
