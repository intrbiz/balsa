package com.intrbiz.balsa.engine.task;

import java.io.Serializable;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.engine.session.BalsaSession;

public class RunnableBalsaTask implements BalsaTask, Serializable
{
    private static final long serialVersionUID = 1L;
    
    private final Runnable task;
    
    public RunnableBalsaTask(final Runnable task)
    {
        this.task = task;
    }
    
    public Runnable task()
    {
        return this.task;
    }
    
    @Override
    public void run(BalsaApplication application, BalsaSession session, String id)
    {
        try
        {
            // execute the task
            this.task.run();
            // store the result
            session.task(id, new BalsaTaskState().complete(null));
        }
        catch (Exception e)
        {
            // store the error
            session.task(id, new BalsaTaskState().failed(e));
        }
    }
}
