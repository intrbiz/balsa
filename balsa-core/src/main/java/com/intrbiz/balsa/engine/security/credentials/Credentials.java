package com.intrbiz.balsa.engine.security.credentials;

/**
 * Credentials are used to authenticate a principal
 */
public interface Credentials
{    
    /**
     * Destroy the credentials, they are no longer needed
     */
    void release();
}
