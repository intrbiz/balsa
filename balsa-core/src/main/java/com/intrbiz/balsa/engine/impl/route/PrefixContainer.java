package com.intrbiz.balsa.engine.impl.route;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.intrbiz.balsa.listener.BalsaRequest;

/**
 * A container of routes with a common prefix
 * 
 */
public class PrefixContainer implements Comparable<PrefixContainer>
{
    private final String prefix;

    private List<RouteEntry> routes = new LinkedList<RouteEntry>();
    
    private final RouteEngineImpl engine;

    public PrefixContainer(String prefix, RouteEngineImpl engine)
    {
        super();
        this.prefix = prefix;
        this.engine = engine;
    }
    
    public RouteEngineImpl getEngine()
    {
        return this.engine;
    }
    
    public String getPrefix()
    {
        return this.prefix;
    }
    
    public List<RouteEntry> getRoutes()
    {
        return this.routes;
    }

    public void registerRoute(RouteEntry handler)
    {
        this.routes.add(handler);
        Collections.sort(this.routes);
    }

    /**
     * Match this prefix to the request
     * @param request
     * @return
     * returns boolean
     */
    public boolean match(BalsaRequest request)
    {
        return request.getPathInfo().startsWith(this.prefix);
    }

    /**
     * Get the route handler for the request
     * @param request
     * @return
     * returns RouteHandler
     */
    public RouteEntry getHandler(BalsaRequest request)
    {
        for (RouteEntry route : this.routes)
        {
            if ((! route.isExceptionHandler()) && route.match(request)) return route;
        }
        return null;
    }
    
    public RouteEntry getExceptionHandler(BalsaRequest request, Throwable t)
    {
        for (RouteEntry route : this.routes)
        {
            if (route.isExceptionHandler() && route.matchException(request, t)) return route;
        }
        return null;
    }

    public int compareTo(PrefixContainer o)
    {
        return o.prefix.length() - prefix.length();
    }

    public String toString()
    {
        return "Prefix: " + this.prefix;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        PrefixContainer other = (PrefixContainer) obj;
        if (prefix == null)
        {
            if (other.prefix != null) return false;
        }
        else if (!prefix.equals(other.prefix)) return false;
        return true;
    }
}
