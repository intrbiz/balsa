package com.intrbiz.balsa.engine.route;

import com.intrbiz.balsa.BalsaContext;

public abstract class RouteExecutor<R extends Router>
{
    protected final R router;
    
    public RouteExecutor(R router)
    {
        super();
        this.router = router;
    }
    
    public abstract void execute(BalsaContext context) throws Throwable;
}
