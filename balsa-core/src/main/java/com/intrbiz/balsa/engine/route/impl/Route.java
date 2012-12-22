package com.intrbiz.balsa.engine.route.impl;

import java.lang.reflect.Method;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.metadata.Catch;

public class Route implements Comparable<Route>
{
    private final String method;

    private final String pattern;

    private final boolean regex;

    private final String[] as;

    private final Method handler;
    
    private final Router router;
    
    private Class<? extends Throwable>[] exceptions = null;
    
    private int order = 0;

    public Route(String method, String pattern, boolean regex, String[] as, Method handler, Router router)
    {
        this.method = method.toUpperCase();
        this.pattern = pattern;
        this.regex = regex;
        this.as = as;
        this.handler = handler;
        this.router = router;
    }

    public String getMethod()
    {
        return method;
    }

    public String getPattern()
    {
        return pattern;
    }

    public boolean isRegex()
    {
        return regex;
    }

    public String[] getAs()
    {
        return as;
    }

    public Method getHandler()
    {
        return handler;
    }
    
    public Router getRouter()
    {
        return this.router;
    }
    
    public boolean isExceptionHandler()
    {
        return this.exceptions != null;
    }
    
    public Class<? extends Throwable>[] getExceptions()
    {
        return this.exceptions;
    }
    
    public void exceptions(Catch exceptions)
    {
        this.exceptions = exceptions.value();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Route other = (Route) obj;
        if (method == null)
        {
            if (other.method != null) return false;
        }
        else if (!method.equals(other.method)) return false;
        if (pattern == null)
        {
            if (other.pattern != null) return false;
        }
        else if (!pattern.equals(other.pattern)) return false;
        return true;
    }
    
    public int getOrder()
    {
        return this.order;
    }
    
    public void setOrder(int order)
    {
        this.order = order;
    }

    @Override
    public int compareTo(Route o)
    {
        return ((o.isRegex() ? 5000 : 0) + (this.isExceptionHandler() ? 10000 : 0) + this.order) - ((o.isRegex() ? 5000 : 0) + (o.isExceptionHandler() ? 10000 : 0) + o.order);
    }
    
    public String toString()
    {
        return "Route: " + this.getMethod() + " " + this.getPattern() + " => " + this.getHandler();
    }
}
