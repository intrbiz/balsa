package com.intrbiz.balsa.engine.impl.task;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Function;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.TaskEngine;
import com.intrbiz.balsa.engine.impl.AbstractBalsaEngine;
import com.intrbiz.balsa.engine.impl.util.DefaultHazelcastFactory;

public class HazelcastTaskEngine extends AbstractBalsaEngine implements TaskEngine
{
    public static final String BALSA_TASK_EXECUTOR = "balsa.executor.task";
    
    private final Function<String, HazelcastInstance> hazelcastInstanceSupplier;
    
    private HazelcastInstance hazelcastInstance;
    
    private IExecutorService executor;
    
    public HazelcastTaskEngine(Function<String, HazelcastInstance> hazelcastInstanceSupplier)
    {
        super();
        this.hazelcastInstanceSupplier = hazelcastInstanceSupplier;
    }
    
    public HazelcastTaskEngine(HazelcastInstance hazelcastInstance)
    {
        this((instanceName) -> hazelcastInstance);
    }
    
    public HazelcastTaskEngine()
    {
        this(new DefaultHazelcastFactory());
    }

    @Override
    public String getEngineName()
    {
        return "Hazelcast-Balsa-Task-Engine";
    }
    
    @Override
    public void start() throws BalsaException
    {
        super.start();
        try
        {
            // Get our hazelcast instance
            this.hazelcastInstance = this.hazelcastInstanceSupplier.apply(this.getBalsaApplication().getInstanceName());
            // create the executor
            this.executor = this.hazelcastInstance.getExecutorService(BALSA_TASK_EXECUTOR);
        }
        catch (Exception e)
        {
            throw new BalsaException("Failed to start Hazelcast Task Engine", e);
        }
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
