package com.intrbiz.balsa.engine.security;

import java.security.Principal;

import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.crypto.cookie.CookieBaker;
import com.intrbiz.crypto.cookie.CryptoCookie;

/**
 * Additional extensions for security engines which handle token based authentications
 */
public interface TokenSecurityEngine
{
    /**
     * Get the CookieBaker which can decode the crypto cookie tokens
     */
    CookieBaker getBaker();
    
    /**
     * Validate the given access token.
     * 
     * @param token the original string token passed by the application to authenticate with
     * @param cookie the parsed CryptoCookie value of the token, containing information such as the expiry time
     * @param principal the Principal that is represented by the token
     * @param requiredFlags the flags which must be set on the cookie as requested by the application
     * @throws BalsaSecurityException if the token is considered invalid
     */
    void validateAccessToken(String token, CryptoCookie cookie, Principal principal, CryptoCookie.Flag[] requiredFlags) throws BalsaSecurityException;
    
    /**
     * Map the given Principal to a token
     */
    byte[] tokenForPrincipal(Principal principal);
    
    /**
     * Map the given token to a Principal
     */
    Principal principalForToken(byte[] token);
}