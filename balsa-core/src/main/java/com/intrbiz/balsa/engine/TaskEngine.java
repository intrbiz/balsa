package com.intrbiz.balsa.engine;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.engine.task.BalsaTaskRunner;

/**
 * Execute long running tasks in the background
 */
public interface TaskEngine extends BalsaEngine
{   
    // Direct task execution
    
    /**
     * Execute a random runnable task
     */
    void execute(Runnable task);
    
    /**
     * Execute a callable task returning a future to get the result
     */
    <T> Future<T> submit(Callable<T> task);
    
    /**
     * Execute a runnable task returning a future to await execution
     */
    Future<?> submit(Runnable task);
    
    /**
     * Execute a runnalbe task returning a future to await execution which will 
     * return the given result
     */
    <T> Future<T> submit(Runnable task, T result);
    
    // polled task execution
    
    /**
     * Execute a task within a restricted Balsa context
     * @param task the task to execute
     * @param id the task id
     * @return the task id
     */
    default String executeTask(Callable<?> task, String id)
    {
        this.execute(new BalsaTaskRunner(BalsaContext.get().session().id(), id, task));
        return id;
    }
    
    /**
     * Execute a task within a restricted Balsa context
     * @param task the task to execute
     * @return the task id
     */
    default String executeTask(Callable<?> task)
    {
        return this.executeTask(task, UUID.randomUUID().toString());
    }
}
