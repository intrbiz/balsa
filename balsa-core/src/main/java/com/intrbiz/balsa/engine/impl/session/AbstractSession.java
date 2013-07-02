package com.intrbiz.balsa.engine.impl.session;

import java.security.Principal;

import com.intrbiz.balsa.engine.session.BalsaSession;

public abstract class AbstractSession implements BalsaSession
{
    private Principal currentPrincipal;
    
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
        this.currentPrincipal = principal;
    }

    @Override
    public Principal currentPrincipal()
    {
        return this.currentPrincipal;
    }
}
