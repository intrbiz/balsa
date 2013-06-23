package com.intrbiz.balsa.engine;

import java.security.Principal;

import com.intrbiz.balsa.engine.security.Credentials;
import com.intrbiz.balsa.error.BalsaSecurityException;

/**
 * Handle authentication and authorisation
 */
public interface SecurityEngine extends BalsaEngine
{   
    /**
     * Authenticate a principal using the given credentials
     * @param credentials the credentials to authenticate with
     * @return the authenticated principal
     * @throws BalsaSecurityException if the principal does not exist or could not be authenticated
     */
    Principal authenticate(Credentials credentials) throws BalsaSecurityException;
    
    /**
     * Check that the given principal has the given permissions
     * @param principal the principal
     * @param permission the permission the principal must have been granted
     * @return true if the principal has the permission, otherwise false
     */
    boolean check(Principal principal, String permission);
}
