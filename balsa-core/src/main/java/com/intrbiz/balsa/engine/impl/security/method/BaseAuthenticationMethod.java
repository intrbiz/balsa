package com.intrbiz.balsa.engine.impl.security.method;

import com.intrbiz.balsa.engine.security.credentials.Credentials;
import com.intrbiz.balsa.engine.security.method.AuthenticationMethod;

/**
 * Skeleton authentication method
 */
public abstract class BaseAuthenticationMethod<T extends Credentials> implements AuthenticationMethod<T>
{
    protected final Class<T> credentialsType;
    
    protected final String name;
    
    protected BaseAuthenticationMethod(Class<T> credentialsType, String name)
    {
        super();
        this.credentialsType = credentialsType;
        this.name = name;
    }

    @Override
    public String name()
    {
        return this.name;
    }

    @Override
    public boolean isValidFor(Credentials credentials)
    {
        return this.credentialsType.isInstance(credentials);
    }
}
