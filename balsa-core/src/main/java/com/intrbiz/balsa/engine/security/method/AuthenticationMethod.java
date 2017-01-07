package com.intrbiz.balsa.engine.security.method;

import java.security.Principal;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.SecurityEngine;
import com.intrbiz.balsa.engine.security.AuthenticationState;
import com.intrbiz.balsa.engine.security.challenge.AuthenticationChallenge;
import com.intrbiz.balsa.engine.security.credentials.Credentials;
import com.intrbiz.balsa.error.BalsaSecurityException;

/**
 * A specific authentication method: password, token, etc
 */
public interface AuthenticationMethod<T extends Credentials>
{
    static final String PASSWORD = "password";
    
    static final String TOKEN = "token";
    
    static final String BACKUP_CODE = "backup_code";
    
    static final String HOTP = "hotp";
    
    static final String U2F = "u2f";
    
    /**
     * The name of this authentication method: eg: password, token, u2f, backup_code, etc
     */
    String name();
    
    /**
     * Setup this authentication method when it is registered with the authentication method,
     * this will also get called when things like the application security key is changed
     * @param engine the security engine this authentication method is registered with
     */
    void setup(SecurityEngine engine) throws BalsaException;
    
    /**
     * Is this authentication method valid for the given credentials, this should return true 
     * only when this authentication method can authenticate using the given credentials.
     */
    boolean isValidFor(Credentials credentials);
    
    /**
     * Using the given credentials and current authentication state attempt to authenticate a principal.
     * On successful authentication the principal and any additional metadata is returned.  On an 
     * unsuccessful authentication a BalsaSecurityException MUST be thrown.
     * @param state the current immutable authentication state
     * @param credentials the credentials to authenticate with
     * @return the authenticated principal and any additional metadata
     * @throws BalsaSecurityException when the credentials are invalid, the principal does not exist or something else goes wrong
     */
    AuthenticatedPrincipal authenticate(AuthenticationState state, T credentials) throws BalsaSecurityException;
    
    /**
     * Verify that the provided credentials are valid for the currently authenticated principal
     * @param state the current immutable authentication state
     * @param credentials the credentials to authenticate with
     * @throws BalsaSecurityException when the credentials are invalid for the current principal
     */
    default void verify(AuthenticationState state, T credentials) throws BalsaSecurityException
    {
        AuthenticatedPrincipal authedPrincipal = this.authenticate(state, credentials);
        if (! authedPrincipal.getPrincipal().equals(state.currentPrincipal())) throw new BalsaSecurityException("The given credentials do not match the current principal");
    }
    
    /**
     * Generate an authentication
     * @param principal the principal to generate the challenge for
     * @return the authentication challenge or null if not applicable
     * @throws BalsaSecurityException if the authentication challenge cannot be generated
     */
    AuthenticationChallenge generateAuthenticationChallenge(Principal principal) throws BalsaSecurityException;
    
}
