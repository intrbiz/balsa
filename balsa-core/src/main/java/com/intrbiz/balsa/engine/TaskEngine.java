package com.intrbiz.balsa.engine;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Execute long running tasks in the background
 */
public interface TaskEngine extends BalsaEngine
{   
    void execute(Runnable task);
    
    <T> Future<T> submit(Callable<T> task);
    
    Future<?> submit(Runnable task);
    
    <T> Future<T> submit(Runnable task, T result);
}
