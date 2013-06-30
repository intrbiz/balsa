package com.intrbiz.balsa.engine;

import java.security.Principal;

import com.intrbiz.balsa.engine.security.Credentials;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.crypto.SecretKey;

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
    
    
    /*
     * Token based access building blocks
     */
    
    /**
     * Set the key that is used to sign access tokens
     * @param key
     */
    void applicationKey(SecretKey key);
    
    /**
     * They key that is used to sign access tokens
     * @return
     */
    SecretKey getApplicationKey();
    
    /**
     * Generate a token which can be used to validate a request
     * @return
     */
    String generateAccessToken();
    
    /**
     * Generate a token which can be used to validate a request
     * @param expiresAt - the time (System.currentTimeMillis()) at which this token will expire
     * @return
     */
    String generateAccessToken(long expiresAt);
    
    /**
     * Generate a token which can be used to validate a request of the given URL
     * @param url - the url which the token is valid for
     * @return
     */
    String generateAccessTokenForURL(String url);
    
    /**
     * Generate a token which can be used to validate a request of the given URL
     * @param url - the url which the token is valid for
     * @param expiresAt - the time (System.currentTimeMillis()) at which this token will expire 
     * @return
     */
    String generateAccessTokenForURL(String url, long expiresAt);
    
    /**
     * Check if the given access token is valid
     * @param token
     * @return
     */
    boolean validAccess(String token);
    
    /**
     * Check if the given access token is valid for the given URL
     * @param url
     * @param token
     * @return
     */
    boolean validAccessForURL(String url, String token);
}
