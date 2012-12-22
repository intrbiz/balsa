package com.intrbiz.balsa.engine.route;

import com.intrbiz.balsa.BalsaContext;

public interface RouteExecutor
{
    public void execute(BalsaContext context) throws Throwable;
}
