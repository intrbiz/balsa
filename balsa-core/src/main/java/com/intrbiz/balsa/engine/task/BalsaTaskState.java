package com.intrbiz.balsa.engine.task;

import java.io.Serializable;

public class BalsaTaskState implements Serializable
{
    private static final long serialVersionUID = 1L;

    private volatile boolean complete = false;
    
    private volatile boolean failed = false;
    
    private volatile Object result = null;
    
    private volatile Exception error;
    
    public BalsaTaskState()
    {
        super();
    }
    
    public boolean isComplete()
    {
        return this.complete;
    }
    
    /**
     * Get the result of the task or throw the exception raised should the task 
     * have failed for any reason
     */
    @SuppressWarnings("unchecked")
    public <T> T get() throws Exception
    {
        if (this.failed) throw this.error;
        return (T) this.result;
    }
    
    public Exception getError()
    {
        return this.error;
    }
    
    public Object getResult()
    {
        return this.result;
    }
    
    public String toString()
    {
        return "balsa-task-state { complete: " + this.complete + ", failed: " + this.failed + " }";
    }
    
    public BalsaTaskState complete(Object result)
    {
        this.result = result;
        this.complete = true;
        return this;
    }
    
    public BalsaTaskState failed(Exception error)
    {
        this.error = error;
        this.failed = true;
        this.complete = true;
        return this;
    }
}
