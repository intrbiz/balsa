package com.intrbiz.balsa.listener;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.BalsaEngine;
import com.intrbiz.balsa.engine.impl.AbstractBalsaEngine;

public abstract class BalsaListener extends AbstractBalsaEngine implements BalsaEngine
{
    public static final int DEFAULT_POOL_SIZE = 16;

    private int port;

    private int poolSize;

    private BalsaProcessor processor;

    protected BalsaListener(int port)
    {
        super();
        this.port = port;
        this.poolSize = DEFAULT_POOL_SIZE;
    }

    public BalsaListener(int port, int poolSize)
    {
        super();
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
    
    public abstract int getDefaultPort();

    public abstract void start() throws BalsaException;

    public abstract void shutdown();

    public abstract void stop();
}
