package com.intrbiz.balsa.engine.impl.session;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.codahale.metrics.Timer;
import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.engine.security.AuthenticationState;
import com.intrbiz.balsa.engine.session.BalsaSession;

public class SimpleSession implements BalsaSession
{
    private volatile long lastAccess;
    
    private final BalsaApplication application;

    private final ConcurrentMap<String, Object> vars;

    private final ConcurrentMap<String, Object> model;
    
    private final Timer.Context timer;
    
    private final SimpleAuthenticationState authenticationState;

    protected String id;

    public SimpleSession(BalsaApplication application, String id, Timer.Context timer)
    {
        super();
        this.id = id;
        this.lastAccess = System.currentTimeMillis();
        this.application = application;
        this.timer = timer;
        this.vars = new ConcurrentHashMap<String, Object>(20, 0.75F, Runtime.getRuntime().availableProcessors());
        this.model = new ConcurrentHashMap<String, Object>(20, 0.75F, Runtime.getRuntime().availableProcessors());
        this.authenticationState = new SimpleAuthenticationState();
    }
    
    @Override
    public String id()
    {
        return this.id;
    }
    
    @Override
    public AuthenticationState authenticationState()
    {
        return this.authenticationState;
    }
    
    public void access()
    {
        this.lastAccess = System.currentTimeMillis();
    }
    
    public long lastAccess()
    {
        return this.lastAccess;
    }
    
    Timer.Context getTimer()
    {
        return this.timer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getVar(String name)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        return (T) this.vars.get(name);
    }

    @Override
    public <T> T putVar(String name, T object)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        if (object != null) 
            this.vars.put(name, object);
        else
            this.vars.remove(name);
        return object;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getModel(String name)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        // find the bean
        return (T) this.model.get(name);
    }

    public <T> T putModel(String name, T model)
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
            this.application.destroyModel(bean);
        }
    }
}
