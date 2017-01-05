package com.intrbiz.balsa.engine.impl.security;

import java.security.Principal;

import com.intrbiz.balsa.error.BalsaSecurityException;

/**
 * A stub security engine which does very little, if anything
 */
public class SecurityEngineImpl extends BaseSecurityEngine
{
    public SecurityEngineImpl()
    {
        super();
    }

    @Override
    public byte[] tokenForPrincipal(Principal principal)
    {
        return null;
    }

    @Override
    public Principal principalForToken(byte[] token)
    {
        return null;
    }

    @Override
    public Principal doPasswordLogin(String username, char[] password) throws BalsaSecurityException
    {
        return null;
    }

    @Override
    public boolean isTwoFactorAuthenticationRequiredForPrincipal(Principal principal)
    {
        return false;
    }

    @Override
    public boolean check(Principal principal, String permission)
    {
        return false;
    }

    @Override
    public boolean check(Principal principal, String permission, Object object)
    {
        return false;
    }
}
