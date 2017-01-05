package com.intrbiz.balsa.engine.security;

import java.security.Principal;
import java.util.Map;

import com.intrbiz.balsa.engine.security.challenge.AuthenticationChallenge;
import com.intrbiz.balsa.engine.security.info.AuthenticationInfo;

/**
 * The authentication data required for a Balsa application.  This authentication 
 * state is stored in a a Balsa session and used during the authentication phases 
 * of an application and used for permissions processing.
 *
 */
public interface AuthenticationState
{
    /**
     * No authentication is currently happening or has not happened
     */
    default boolean isNotAuthenticated()
    {
        return this.currentPrincipal() == null && this.authenticatingPrincipal() == null;
    }
    
    /**
     * Authentication is currently in progress
     */
    default boolean isAuthenticating()
    {
        return this.authenticatingPrincipal() != null;
    }
    
    /**
     * Do we have an authenticated principal
     */
    default boolean isAuthenticated()
    {
        return this.currentPrincipal() != null;
    }
    
    /**
     * When did the current authentication start.
     * If no authentication is in progress -1 will be returned.
     */
    long authenticationStartedAt();
    
    /**
     * Get the principal which is currently in the process of authenticating, this is used when two factor authentication is happening.
     * @return the principal
     */
    <T extends Principal> T authenticatingPrincipal();
    
    /**
     * Get the principal which is currently authenticated, this is the principal which is strongly authenticated having provides all factors 
     * as required by the security engine.
     * @return the principal
     */
    <T extends Principal> T currentPrincipal();
    
    /**
     * Update the authentication state with the current authentication response.
     * 
     * For a response which is complete, the following will be updated:
     * 1) currentPrincipal() is set to response.getPrincipal()
     * 2) authenticatingPrincipal() is reset to null
     * 3) authenticationStartedAt() is reset to -1L
     * 4) info() is set to response.getInfo()
     * 5) challenge() is reset to null
     * 
     * For a response which is not complete and a second factor needs to be provided:
     * 1) currentPrincipal() is reset to null
     * 2) authenticatingPrincipal() is set to response.getPrincipal()
     * 3) authenticationStartedAt() is set to System.currentTimeMillis()
     * 4) info() is reset to null
     * 5) challenge() is set to response.getChallenge()
     * 
     */
    AuthenticationResponse update(AuthenticationResponse response);
    
    /**
     * Reset this authentication state, this deauthenticates 
     * the current principal, the following happens:
     * 1) currentPrincipal() is reset to null
     * 2) authenticatingPrincipal() is reset to null
     * 3) authenticationStartedAt() is reset to -1L
     * 4) info() is reset to null
     * 5) challenge() is reset to null 
     */
    AuthenticationState reset();
    
    /**
     * Get the security engine specific authentication information for 
     * the currently authenticated principal
     */
    AuthenticationInfo info();
    
    /**
     * Get the security engine specific authentication challenge for 
     * this currently authenticating principal
     */
    Map<String, AuthenticationChallenge> challenges();
}
