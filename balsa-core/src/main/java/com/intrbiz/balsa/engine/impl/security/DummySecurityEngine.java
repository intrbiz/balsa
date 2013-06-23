package com.intrbiz.balsa.engine.impl.security;

import java.security.Principal;

import com.intrbiz.balsa.engine.SecurityEngine;
import com.intrbiz.balsa.engine.impl.AbstractBalsaEngine;
import com.intrbiz.balsa.engine.security.Credentials;
import com.intrbiz.balsa.error.BalsaSecurityException;

public class DummySecurityEngine extends AbstractBalsaEngine implements SecurityEngine
{
    public DummySecurityEngine()
    {
        super();
    }

    @Override
    public String getEngineName()
    {
        return "Dummy Security Engine";
    }

    @Override
    public Principal authenticate(Credentials credentials) throws BalsaSecurityException
    {
        // don't do anything
        throw new BalsaSecurityException("Authentication is not supported");
    }

    @Override
    public boolean check(Principal principal, String permission)
    {
        return false;
    }
}
