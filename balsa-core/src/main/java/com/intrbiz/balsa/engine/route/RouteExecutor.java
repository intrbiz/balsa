package com.intrbiz.balsa.engine.route;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.intrbiz.balsa.BalsaContext;

public abstract class RouteExecutor<R extends Router>
{
    protected final R router;
    
    protected final Method handler;
    
    protected final Annotation[] annotations;
    
    protected final Class<?>[] parameterTypes;
    
    protected final Annotation[][] parameterAnnotations;
    
    protected final int arity;
    
    public RouteExecutor(R router, Method handler)
    {
        super();
        this.router = router;
        this.handler = handler;
        this.annotations = handler.getAnnotations();
        this.parameterTypes = handler.getParameterTypes();
        this.parameterAnnotations = handler.getParameterAnnotations();
        this.arity = this.parameterTypes.length;
    }
    
    public int getArity()
    {
        return this.arity;
    }
    
    public Annotation[] getParameterAnnotations(int index)
    {
        return this.parameterAnnotations[index];
    }
    
    public abstract void execute(BalsaContext context) throws Throwable;
}
