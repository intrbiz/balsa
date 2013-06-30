package com.intrbiz.balsa.engine.impl.security;

import static com.intrbiz.util.Hash.asUTF8;
import static com.intrbiz.util.Hash.sha256;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.intrbiz.balsa.engine.SecurityEngine;
import com.intrbiz.balsa.engine.impl.AbstractBalsaEngine;
import com.intrbiz.balsa.engine.security.Credentials;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.crypto.SecretKey;
import com.intrbiz.crypto.cookie.CookieBaker;
import com.intrbiz.crypto.cookie.CryptoCookie;
import com.intrbiz.crypto.cookie.CryptoCookie.Flag;

public class SecurityEngineImpl extends AbstractBalsaEngine implements SecurityEngine
{
    protected SecretKey applicationKey = SecretKey.generate();
    
    protected int tokenLength;
    
    protected long lifetime;
    
    protected TimeUnit lifetimeUnit;
    
    protected Flag[] flags;
    
    protected CookieBaker baker;
    
    public SecurityEngineImpl()
    {
        super();
        // defaults
        this.tokenLength = 32;
        this.lifetime = 1;
        this.lifetimeUnit = TimeUnit.HOURS;
        this.flags = new Flag[0];
        this.setupBaker();
    }
    
    protected void setupBaker()
    {
        this.baker = new CookieBaker(this.applicationKey, this.tokenLength, this.lifetime, this.lifetimeUnit, this.flags);
    }

    @Override
    public String getEngineName()
    {
        return "Balsa-Security-Engine";
    }

    @Override
    public Principal authenticate(Credentials credentials) throws BalsaSecurityException
    {
        throw new BalsaSecurityException("Authentication is not supported");
    }

    @Override
    public boolean check(Principal principal, String permission)
    {
        return false;
    }
    
    

    @Override
    public void applicationKey(SecretKey key)
    {
        if (key != null)
        {
            this.applicationKey = key;
            this.setupBaker();
        }
    }

    @Override
    public SecretKey getApplicationKey()
    {
        return this.applicationKey;
    }

    @Override
    public String generateAccessToken()
    {
        return this.baker.bake().toString();
    }

    @Override
    public String generateAccessToken(long expiresAt)
    {
        return this.baker.bake(expiresAt).toString();
    }

    @Override
    public String generateAccessTokenForURL(String url)
    {
        return this.baker.bake(sha256(asUTF8(url))).toString();
    }

    @Override
    public String generateAccessTokenForURL(String url, long expiresAt)
    {
        return this.baker.bake(sha256(asUTF8(url)), expiresAt).toString();
    }

    @Override
    public boolean validAccess(String token)
    {
        // parse and verify the cookie
        try
        {
            CryptoCookie cookie = CryptoCookie.fromString(token);
            return this.baker.verify(cookie);
        }
        catch (IOException e)
        {
        }
        return false;
    }

    @Override
    public boolean validAccessForURL(String url, String token)
    {
        // parse and verify the cookie
        try
        {
            CryptoCookie cookie = CryptoCookie.fromString(token);
            if (!this.baker.verify(cookie)) return false;
            // verify the URL
            return Arrays.equals(cookie.getToken(), sha256(asUTF8(url))); 
        }
        catch (IOException e)
        {
        }
        return false;
    }
}
