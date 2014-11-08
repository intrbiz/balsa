package com.intrbiz.balsa.engine.task;

import java.io.Serializable;
import java.util.concurrent.Callable;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.engine.session.BalsaSession;

public class CallableBalsaTask<T> implements BalsaTask, Serializable
{
    private static final long serialVersionUID = 1L;
    
    private final Callable<T> task;
    
    public CallableBalsaTask(final Callable<T> task)
    {
        this.task = task;
    }
    
    public Callable<T> task()
    {
        return this.task;
    }
    
    @Override
    public void run(BalsaApplication application, BalsaSession session, String id)
    {
        try
        {
            // execute the task
            T result = this.task.call();
            // store the result
            session.task(id, new BalsaTaskState().complete(result));
        }
        catch (Exception e)
        {
            // store the error
            session.task(id, new BalsaTaskState().failed(e));
        }
    }
}
