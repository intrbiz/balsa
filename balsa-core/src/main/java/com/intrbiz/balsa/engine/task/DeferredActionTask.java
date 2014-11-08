package com.intrbiz.balsa.engine.task;

import java.io.Serializable;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.session.BalsaSession;
import com.intrbiz.express.action.ActionHandler;

public class DeferredActionTask implements BalsaTask, Serializable
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
    public void run(BalsaApplication application, BalsaSession session, String id)
    {
        try
        {
            // get the action
            ActionHandler handler = application.action(this.action);
            if (handler == null) throw new BalsaException("The action " + action + " does not exist");
            // invoke the action
            Object result = handler.act(this.arguments);
            // store the state
            session.task(id, new BalsaTaskState().complete(result));
        }
        catch (Exception e)
        {
            session.task(id, new BalsaTaskState().failed(e));
        }
    }
}
