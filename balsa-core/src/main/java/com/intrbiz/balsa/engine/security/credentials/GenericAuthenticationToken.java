package com.intrbiz.balsa.engine.security.credentials;

import com.intrbiz.crypto.cookie.CryptoCookie;

/**
 * A generic authentication token which can be used 
 * to authenticate a principal
 */
public class GenericAuthenticationToken implements TokenCredentials
{
    private String token;
    
    private CryptoCookie.Flag[] requiredFlags = null;
    
    public GenericAuthenticationToken(String token, CryptoCookie.Flag... requiredFlags)
    {
        this.token = token;
        this.requiredFlags = requiredFlags;
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

    public CryptoCookie.Flag[] getRequiredFlags()
    {
        return requiredFlags;
    }
}
