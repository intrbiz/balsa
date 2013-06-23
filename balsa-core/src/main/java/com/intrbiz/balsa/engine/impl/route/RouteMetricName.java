package com.intrbiz.balsa.engine.impl.route;

import com.yammer.metrics.core.MetricName;

public class RouteMetricName extends MetricName
{
    private String httpMethod;
    
    private String pattern;
    
    public RouteMetricName(Route route, String metricName)
    {
        super(route.getRouter().getClass(), metricName, route.getHandler().getName());
        this.httpMethod = route.getMethod();
        this.pattern = route.getPattern();
    }
    
    public String getHttpMethod()
    {
        return this.httpMethod;
    }
    
    public String getPattern()
    {
        return this.pattern;
    }
    
    public String toString()
    {
        return super.toString() + ",method=" + this.getHttpMethod() + ",pattern=" + this.getPattern();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((httpMethod == null) ? 0 : httpMethod.hashCode());
        result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        RouteMetricName other = (RouteMetricName) obj;
        if (httpMethod == null)
        {
            if (other.httpMethod != null) return false;
        }
        else if (!httpMethod.equals(other.httpMethod)) return false;
        if (pattern == null)
        {
            if (other.pattern != null) return false;
        }
        else if (!pattern.equals(other.pattern)) return false;
        return true;
    }
}
