package com.intrbiz.balsa.engine.impl.task;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.intrbiz.balsa.engine.TaskEngine;
import com.intrbiz.balsa.engine.impl.AbstractBalsaEngine;

public class TaskEngineImpl extends AbstractBalsaEngine implements TaskEngine
{
    private final ExecutorService executor = Executors.newCachedThreadPool();
    
    public TaskEngineImpl()
    {
        super();
    }

    @Override
    public String getEngineName()
    {
      return "Balsa-Task-Engine";
    }

    @Override
    public void execute(Runnable task)
    {
        this.executor.execute(task);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task)
    {
        return this.executor.submit(task);
    }

    @Override
    public Future<?> submit(Runnable task)
    {
        return this.executor.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result)
    {
        return this.executor.submit(task, result);
    }
}
