package com.intrbiz.balsa.engine.security;

/**
 * A generic authentication token which can be used 
 * to authenticate a principal
 */
public class GenericAuthenticationToken implements TokenCredentials
{
    private String token;
    
    public GenericAuthenticationToken(String token)
    {
        this.token = token;
    }

    @Override
    public void release()
    {
        this.token = null;
    }

    @Override
    public String getToken()
    {
        return this.token;
    }
}
