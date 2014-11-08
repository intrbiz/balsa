package com.intrbiz.balsa.engine;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.engine.task.BalsaTask;
import com.intrbiz.balsa.engine.task.BalsaTaskRunner;
import com.intrbiz.balsa.engine.task.CallableBalsaTask;
import com.intrbiz.balsa.engine.task.RunnableBalsaTask;

/**
 * Execute long running tasks in the background
 */
public interface TaskEngine extends BalsaEngine
{   
    // Direct task execution
    
    void execute(Runnable task);
    
    <T> Future<T> submit(Callable<T> task);
    
    Future<?> submit(Runnable task);
    
    <T> Future<T> submit(Runnable task, T result);
    
    // polled task execution
    
    default String executeTask(BalsaTask task, String id)
    {
        this.execute(new BalsaTaskRunner(BalsaContext.get().session().id(), id, task));
        return id;
    }
    
    default String executeTask(Runnable task, String id)
    {
        return this.executeTask(new RunnableBalsaTask(task), id);
    }
    
    default <T> String executeTask(Callable<T> task, String id)
    {
        return this.executeTask(new CallableBalsaTask<T>(task), id);
    }
    
    default String executeTask(BalsaTask task)
    {
        return this.executeTask(task, UUID.randomUUID().toString());
    }
    
    default String executeTask(Runnable task)
    {
        return this.executeTask(new RunnableBalsaTask(task));
    }
    
    default <T> String executeTask(Callable<T> task)
    {
        return this.executeTask(new CallableBalsaTask<T>(task));
    }
}
