package com.intrbiz.balsa.engine.security.credentials;

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
