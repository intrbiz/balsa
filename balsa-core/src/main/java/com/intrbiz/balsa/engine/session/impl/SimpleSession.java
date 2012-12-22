package com.intrbiz.balsa.engine.session.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.intrbiz.balsa.BalsaApplication;

public class SimpleSession extends AbstractSession
{
    private final BalsaApplication application;

    private final String id;

    private volatile long lastAccess;

    private final ConcurrentMap<String, Object> vars;

    private final ConcurrentMap<String, Object> model;

    public SimpleSession(BalsaApplication application, String id, int poolSize)
    {
        super();
        this.application = application;
        this.id = id;
        this.lastAccess = System.currentTimeMillis();
        this.vars = new ConcurrentHashMap<String, Object>(20, 0.75F, poolSize);
        this.model = new ConcurrentHashMap<String, Object>(20, 0.75F, poolSize);
    }

    public void access()
    {
        this.lastAccess = System.currentTimeMillis();
    }

    @Override
    public String id()
    {
        return this.id;
    }

    @Override
    public long lastAccess()
    {
        return this.lastAccess;
    }

    @Override
    public Object var(String name)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        return this.vars.get(name);
    }

    @Override
    public void var(String name, Object object)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        if (object == null)
        {
            this.vars.remove(name);
        }
        else
        {
            this.vars.put(name, object);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T model(String name, Class<T> type, boolean create)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        // find the bean
        T bean = (T) this.model.get(name);
        if (bean == null && create)
        {
            // create the bean
            bean = this.application.activateModel(type);
            if (bean != null)
            {
                this.model.put(name, bean);
            }
        }
        return bean;
    }

    public <T> T model(String name, T model)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        if (model == null)
        {
            this.model.remove(name);
        }
        else
        {
            this.model.put(name, model);
        }
        return model;
    }

    @Override
    public void removeVar(String name)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        this.vars.remove(name);
    }

    @Override
    public void removeModel(String name)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        this.removeModel(name);
    }

    @Override
    public void deactivate()
    {
        // deactivate all beans
        // return all beans to the providers
        for (Object bean : this.model.values())
        {
            this.application.deactivateModel(bean);
        }
    }
}
