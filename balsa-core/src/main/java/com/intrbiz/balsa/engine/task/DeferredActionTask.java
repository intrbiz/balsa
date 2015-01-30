package com.intrbiz.balsa.engine.task;

import java.io.Serializable;
import java.util.concurrent.Callable;

import static com.intrbiz.balsa.BalsaContext.Balsa;

/**
 * Call an action in a deferred manner
 */
public class DeferredActionTask implements Callable<Object>, Serializable
{
    private static final long serialVersionUID = 1L;
    
    private final String action;
    
    private final Object[] arguments;
    
    public DeferredActionTask(String action, Object[] arguments)
    {
        super();
        this.action = action;
        this.arguments = arguments;
    }
    
    public String getAction()
    {
        return this.action;
    }
    
    public Object[] getArguments()
    {
        return this.arguments;
    }

    @Override
    public Object call() throws Exception
    {
        return Balsa().action(this.action, this.arguments);
    }
}
