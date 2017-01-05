package com.intrbiz.balsa.engine.security;

import java.security.Principal;

import com.intrbiz.balsa.error.BalsaSecurityException;

/**
 * Additional extensions for security engines which handle password authentications
 */
public interface PasswordSecurityEngine
{
    /**
     * Perform password based authentication for the given username and password 
     * returning the corresponding Principal.
     * @param username the username of the principal
     * @param password the password of the principal
     * @return the Principal or null
     */
    Principal doPasswordLogin(String username, char[] password) throws BalsaSecurityException;
}