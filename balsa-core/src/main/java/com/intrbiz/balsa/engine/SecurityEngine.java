package com.intrbiz.balsa.engine;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;

import com.intrbiz.balsa.engine.security.AuthenticationResponse;
import com.intrbiz.balsa.engine.security.AuthenticationState;
import com.intrbiz.balsa.engine.security.challenge.AuthenticationChallenge;
import com.intrbiz.balsa.engine.security.credentials.Credentials;
import com.intrbiz.balsa.engine.security.method.AuthenticationMethod;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.crypto.SecretKey;
import com.intrbiz.crypto.cookie.CryptoCookie;

/**
 * Handle authentication and authorisation
 */
public interface SecurityEngine extends BalsaEngine
{   
    /**
     * Get the authentication methods registered with this security engine
     */
    Collection<AuthenticationMethod<?>> authenticationMethods();
    
    /**
     * Get a specific authentication method
     */
    <T extends AuthenticationMethod<?>> T getAuthenticationMethod(String name);
    
    /**
     * Register an authentication method with this security engine
     * @param method the method to support
     */
    SecurityEngine registerAuthenticationMethod(AuthenticationMethod<?> method);
    
    /**
     * Is the named authentication method registered with this security engine
     * @param name the authentication method name
     */
    boolean isAuthenticationMethodRegistered(String name);
    
    /**
     * The level of validation that is to be applied 
     * when checking if a Principal is valid.
     * 
     * Strong validation asserts additional checks upon 
     * a principal, EG: checking if the account is locked, 
     * the password needs changing, etc.
     * 
     * Weak validation merely asserts that the Principal 
     * is an authenticated user, asserting the minimum of 
     * checks,
     * 
     * The validation level is useful for implementing 
     * quarantine areas, where an authenticated user can 
     * perform some limited actions but not use the full 
     * application.
     * 
     * When checking for a valid principal, Strong validation is 
     * the default.
     */
    public static enum ValidationLevel { STRONG, WEAK };
    
    /**
     * Get the default Principal that a context will be initialised with,
     * this represent a public user.  By default this will return null, 
     * however some implementations may return a dedicated public Principal, 
     * this allows for granular permissions to be applied to the public Principal.
     */
    Principal defaultPrincipal();
    
    /**
     * Authenticate a principal using the given credentials for this life of this request only.  Implementations should be careful to require no state
     * @param state the current authentication state
     * @param credentials the credentials to authenticate with
     * @return the authentication response
     * @throws BalsaSecurityException if the principal does not exist or could not be authenticated
     */
    Principal authenticateRequest(Credentials credentials) throws BalsaSecurityException;
    
    /**
     * Authenticate a principal using the given credentials
     * @param state the current authentication state
     * @param credentials the credentials to authenticate with
     * @return the authentication response
     * @throws BalsaSecurityException if the principal does not exist or could not be authenticated
     */
    AuthenticationResponse authenticate(AuthenticationState state, Credentials credentials) throws BalsaSecurityException;
    
    /**
     * Verify the given credentials are valid for the currently authenticated principal
     * @param state the current authentication state
     * @param credentials the credentials for the principal
     * @throws BalsaSecurityException should the given credentials not be valid for the current principal
     */
    void verify(AuthenticationState state, Credentials credentials) throws BalsaSecurityException;
    
    /**
     * Generate a set of challenges which are needed to authenticate the given 
     * principal via certain authentication methods.  This is a map of the 
     * authentication method name to challenge
     */
    public Map<String, AuthenticationChallenge> generateAuthenticationChallenges(Principal principal);
    
    /**
     * Check that the given principal has the given permissions
     * @param principal the principal
     * @param permission the permission the principal must have been granted
     * @return true if and only if the principal has the permission, otherwise false
     */
    boolean check(Principal principal, String permission);
    
    /**
     * Check that the current user has the given permission over the given object
     * @param permission the permission name
     * @param object the object over which permission must be granted
     * @return true if and only if the current user has the given permission over th given object
     */
    boolean check(Principal principal, String permission, Object object);
    
    /**
     * Check if the given Principal is valid, usually this 
     * just checks that the Principal is not null, however specific 
     * security engine implementations might assert additional checks.
     * @param principal the Principal to validate
     * @param validationLevel how strongly the principal should be validated
     * @return true if the given Principal is valid, otherwise false
     */
    boolean isValidPrincipal(Principal principal, ValidationLevel validationLevel);
    
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
     * Generate a token which can be used to authenticate a Principal at a latter date.  
     * The token will expire after the default configured lifetime of this SecurityEngine.
     * @param principal the Principal that the token will represent
     * @return the token
     */
    String generateAuthenticationTokenForPrincipal(Principal principal, CryptoCookie.Flag... flags);
    
    /**
     * Generate a token which can be used to authenticate a Principal at a latter date.
     * @param principal the Principal that the token will represent
     * @param expiresAt the time (System.currentTimeMillis()) at which this token will expire
     * @return the token
     */
    String generateAuthenticationTokenForPrincipal(Principal principal, long expiresAt, CryptoCookie.Flag... flags);
    
    /**
     * Generate a token which can be used to authenticate a Principal at a latter date, perpetually.
     * Generation of perpetual tokens is valid, however by default they cannot be used for authentication, 
     * this can be overridden by a specific security engine implementation.
     * @param principal the Principal that the token will represent
     * @return the token
     */
    String generatePerpetualAuthenticationTokenForPrincipal(Principal principal, CryptoCookie.Flag... flags);
    
    /**
     * Regenerate a previously generated authentication token, the Principal remains the same 
     * however the expiry time is extended.  A BalsaSecurityException will be thrown if the 
     * given token cannot be extended. 
     * @param token the token to regenerate
     * @return the regenerated token
     */
    String regenerateAuthenticationTokenForPrincipal(String token);

    /**
     * Regenerate a previously generated authentication token, the Principal remains the same 
     * however the expiry time is extended to the given expiry time.  A BalsaSecurityException 
     * will be thrown if the given token cannot be extended. 
     * @param token the token to regenerate
     * @param expiresAt the time (System.currentTimeMillis()) at which this token will expire 
     * @return the regenerated token
     */
    String regenerateAuthenticationTokenForPrincipal(String token, long expiresAt);
    
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
    
    /**
     * Is two factor authentication required for the given principal
     * @param principal the principal which has been authenticated
     * @return yes or no
     */
    boolean isTwoFactorAuthenticationRequiredForPrincipal(Principal principal);
}
