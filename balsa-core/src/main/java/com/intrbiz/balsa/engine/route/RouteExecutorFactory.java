package com.intrbiz.balsa.engine.route;

import java.lang.reflect.Method;


public interface RouteExecutorFactory
{
    /**
     * Is this executor valid for this route handler
     */
    boolean match(Method handler, String[] as, Router router);
    
    /**
     * Get the executor for the route
     */
    RouteExecutor compileExecutor(Method handler, String[] as, Router router);
}
