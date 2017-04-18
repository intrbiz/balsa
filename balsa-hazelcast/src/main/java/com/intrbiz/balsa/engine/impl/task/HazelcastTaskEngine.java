package com.intrbiz.balsa.engine.impl.task;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.intrbiz.Util;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.TaskEngine;
import com.intrbiz.balsa.engine.impl.AbstractBalsaEngine;

public class HazelcastTaskEngine extends AbstractBalsaEngine implements TaskEngine
{
    private HazelcastInstance hazelcastInstance;
    
    private Config hazelcastConfig;
    
    private IExecutorService executor;
    
    public HazelcastTaskEngine()
    {
        super();
    }
    
    public HazelcastTaskEngine(HazelcastInstance hazelcastInstance)
    {
        super();
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public String getEngineName()
    {
        return "Hazelcast-Balsa-Task-Engine";
    }
    
    public HazelcastInstance getHazelcastInstance()
    {
        return this.hazelcastInstance;
    }
    
    @Override
    public void start() throws BalsaException
    {
        super.start();
        try
        {
            if (this.hazelcastInstance == null)
            {
                // setup hazelcast
                String hazelcastConfigFile = Util.coalesceEmpty(System.getProperty("hazelcast.config"), System.getenv("hazelcast_config"));
                if (hazelcastConfigFile != null)
                {
                    // when using a config file, you must configure the balsa.sessions map
                    this.hazelcastConfig = new XmlConfigBuilder(hazelcastConfigFile).build();
                }
                else
                {
                    // setup the default configuration
                    this.hazelcastConfig = new Config();
                }
                // set the instance name
                this.hazelcastConfig.setInstanceName(this.getBalsaApplication().getInstanceName());
                // create the instance
                this.hazelcastInstance = Hazelcast.getOrCreateHazelcastInstance(this.hazelcastConfig);
            }
            // create the executor
            this.executor = this.hazelcastInstance.getExecutorService("balsa-task");
        }
        catch (Exception e)
        {
            throw new BalsaException("Failed to start Hazelcast Task Engine", e);
        }
    }
    
    //

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
