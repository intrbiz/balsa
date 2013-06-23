package com.intrbiz.balsa.engine.security;

/**
 * Credentials which pair a principal name (username) with some form of secret
 */
public interface LoginCredentials extends Credentials
{
    /**
     * The name of the principal to authenticate
     */
    String getPrincipalName();
}
