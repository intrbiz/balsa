package com.intrbiz.balsa.engine.impl;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.BalsaEngine;

/**
 * Basic engine implementation
 */
public class AbstractBalsaEngine implements BalsaEngine
{
    protected BalsaApplication application;
    
    public AbstractBalsaEngine()
    {
        super();
    }

    @Override
    public BalsaApplication getBalsaApplication()
    {
        return this.application;
    }

    @Override
    public void setBalsaApplication(BalsaApplication application)
    {
        this.application = application;
    }

    @Override
    public void start() throws BalsaException
    {
    }

    @Override
    public void stop()
    {
    }
}
