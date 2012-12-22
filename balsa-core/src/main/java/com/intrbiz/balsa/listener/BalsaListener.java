package com.intrbiz.balsa.listener;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.BalsaEngine;
import com.intrbiz.balsa.engine.impl.AbstractBalsaEngine;

public abstract class BalsaListener extends AbstractBalsaEngine implements BalsaEngine
{
    public static final int DEFAULT_PORT = 8090;

    public static final int DEFAULT_POOL_SIZE = 16;

    private int port = DEFAULT_PORT;

    private int poolSize = DEFAULT_POOL_SIZE;

    private BalsaProcessor processor;

    public BalsaListener()
    {
        super();
    }

    public BalsaListener(int port)
    {
        this();
        this.port = port;
    }

    public BalsaListener(int port, int poolSize)
    {
        this();
        this.port = port;
        this.poolSize = poolSize;
    }

    public final int getPort()
    {
        return port;
    }

    public final void setPort(int port)
    {
        this.port = port;
    }

    public final int getPoolSize()
    {
        return poolSize;
    }

    public final void setPoolSize(int poolSize)
    {
        this.poolSize = poolSize;
    }

    public final BalsaProcessor getProcessor()
    {
        return processor;
    }

    public final void setProcessor(BalsaProcessor processor)
    {
        this.processor = processor;
    }

    public abstract void start() throws BalsaException;

    public abstract void shutdown();

    public abstract void stop();
}
