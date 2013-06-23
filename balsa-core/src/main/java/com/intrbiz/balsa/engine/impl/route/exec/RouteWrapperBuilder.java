package com.intrbiz.balsa.engine.impl.route.exec;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.intrbiz.balsa.engine.impl.route.exec.model.ExecutorClass;

public abstract class RouteWrapperBuilder
{   
    public abstract void fromAnnotation(Annotation a);
    
    public abstract ArgumentBuilder<?> argument(Method method, int index, Class<?> arguementType, Annotation[] annotations);
    
    public abstract void compileBefore(ExecutorClass cls);
    
    public abstract void compileAfter(ExecutorClass cls);
    
    
    public void compileAfterBind(ExecutorClass cls)
    {
    }
}
