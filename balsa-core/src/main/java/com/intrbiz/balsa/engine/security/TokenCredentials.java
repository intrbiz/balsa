package com.intrbiz.balsa.engine.security;

/**
 * Authenticate using some kind of token (SSO)
 */
public interface TokenCredentials extends Credentials
{
    /**
     * The authentication token
     */
    Object getToken();
}
