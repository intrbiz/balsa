package com.intrbiz.balsa.engine.impl.route;

import com.intrbiz.gerald.witchcraft.Witchcraft;

public class RouteMetric
{    
    public static final String name(Route route, String metricName)
    {
        return Witchcraft.name(route.getRouter().getClass(), route.getHandler().getName(), "[method=" + route.getMethod() + "; route=" + route.getPattern() + "]", metricName);
    }
}
