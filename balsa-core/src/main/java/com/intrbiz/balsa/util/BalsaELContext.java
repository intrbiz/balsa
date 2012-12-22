package com.intrbiz.balsa.util;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.express.AbstractELContext;
import com.intrbiz.express.operator.Decorator;
import com.intrbiz.express.operator.Function;

public abstract class BalsaELContext extends AbstractELContext
{   
    public BalsaELContext()
    {
        super();
    }

    @Override
    public Function getCustomFunction(String name)
    {
        return null;
    }

    @Override
    public Decorator getCustomDecorator(String name, Class<?> entityType)
    {
        return null;
    }
    
    public abstract BalsaApplication getApplication();
}
