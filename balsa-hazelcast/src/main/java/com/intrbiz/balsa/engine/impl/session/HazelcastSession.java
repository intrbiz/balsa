package com.intrbiz.balsa.engine.impl.session;

import static com.intrbiz.balsa.BalsaContext.*;

import java.io.Serializable;
import java.security.Principal;
import java.util.concurrent.ConcurrentMap;

import com.intrbiz.balsa.engine.session.BalsaSession;
import com.intrbiz.balsa.error.BalsaInternalError;

public class HazelcastSession implements BalsaSession, Serializable
{
    private static final long serialVersionUID = 1L;

    protected String sessionId;

    public HazelcastSession(final String id)
    {
        super();
        this.sessionId = id;
    }

    public HazelcastSession()
    {
        super();
    }

    @Override
    public final String id()
    {
        return this.sessionId;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T var(String name, Class<T> type)
    {
        Object var = this.var(name);
        if (type.isInstance(var)) return (T) var;
        return null;
    }

    public <T> T model(String name, Class<T> type)
    {
        return this.model(name, type, true);
    }

    @Override
    public void setCurrentPrincipal(Principal principal)
    {
        if (principal == null)
        {
            this.getAttributeMap().remove(this.principalId());
        }
        else
        {
            this.getAttributeMap().put(this.principalId(), principal);
        }
    }

    @Override
    public Principal currentPrincipal()
    {
        return (Principal) this.getAttributeMap().get(this.principalId());
    }

    public Object getEntity(String name, Object source)
    {
        Object value = this.model(name);
        if (value == null) value = this.var(name);
        return value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T var(String name)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        return (T) this.getAttributeMap().get(this.varId(name));
    }

    @Override
    public <T> T var(String name, T object)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        if (object == null)
        {
            this.getAttributeMap().remove(this.varId(name));
        }
        else
        {
            this.getAttributeMap().put(this.varId(name), object);
        }
        return object;
    }

    @Override
    public void removeVar(String name)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        this.getAttributeMap().remove(this.varId(name));
    }

    @Override
    public <T> T model(String name, Class<T> type, boolean create)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        T model = this.model(name);
        if (model == null && create) { throw new BalsaInternalError("When using clustered sessions pooled model objects cannot be used!"); }
        return model;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T model(String name)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        return (T) this.getAttributeMap().get(this.modelId(name));
    }

    @Override
    public <T> T model(String name, T model)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        if (model == null)
        {
            this.getAttributeMap().remove(this.modelId(name));
        }
        else
        {
            this.getAttributeMap().put(this.modelId(name), model);
        }
        return model;
    }

    @Override
    public void removeModel(String name)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        this.getAttributeMap().remove(this.modelId(name));
    }

    @Override
    public void deactivate()
    {
    }

    // util

    protected String varId(String name)
    {
        return this.id() + ".var." + name;
    }

    protected String modelId(String name)
    {
        return this.id() + ".model." + name;
    }
    
    protected String principalId()
    {
        return this.id() + ".principal";
    }
    
    protected ConcurrentMap<String, Object> getAttributeMap()
    {
        return ((HazelcastSessionEngine) Balsa().app().getSessionEngine()).getAttributeMap();
    }
}
