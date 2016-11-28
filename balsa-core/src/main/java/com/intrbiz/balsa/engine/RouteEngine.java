package com.intrbiz.balsa.engine;

import java.util.List;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.route.Router;

public interface RouteEngine extends BalsaEngine
{   
    void route(BalsaContext context) throws Throwable;
    
    void routeException(BalsaContext context, Throwable t) throws Throwable;
    
    List<Router<?>> getRouters();
    
    void router(Router<?> router) throws BalsaException;
}
