package com.intrbiz.balsa.engine.impl.security.method;

import java.io.IOException;
import java.security.Principal;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.SecurityEngine;
import com.intrbiz.balsa.engine.security.AuthenticationState;
import com.intrbiz.balsa.engine.security.TokenSecurityEngine;
import com.intrbiz.balsa.engine.security.challenge.AuthenticationChallenge;
import com.intrbiz.balsa.engine.security.credentials.GenericAuthenticationToken;
import com.intrbiz.balsa.engine.security.method.AuthenticatedPrincipal;
import com.intrbiz.balsa.engine.security.method.AuthenticationMethod;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.crypto.cookie.CryptoCookie;

/**
 * Provide token based authentication.
 * 
 * Note: this authentication provided must be registered 
 * against a security engine which implements SecurityEngine.TokenSecurityEngine
 */
public class TokenAuthenticationMethod extends BaseAuthenticationMethod<GenericAuthenticationToken>
{
    private static Logger logger = Logger.getLogger(TokenAuthenticationMethod.class);
    
    protected TokenSecurityEngine securityEngine;
    
    public TokenAuthenticationMethod()
    {
        super(GenericAuthenticationToken.class, AuthenticationMethod.TOKEN);
    }
    
    @Override
    public void setup(SecurityEngine engine) throws BalsaException
    {
        if (! (engine instanceof TokenSecurityEngine))
            throw new BalsaException("The token authentication method can only be registered against a security engine which implements SecurityEngine.TokenSecurityEngine");
        this.securityEngine = (TokenSecurityEngine) engine;
    }

    @Override
    public AuthenticatedPrincipal authenticate(AuthenticationState state, GenericAuthenticationToken gt) throws BalsaSecurityException
    {
        String token = gt.getToken();
        logger.debug("Authenticating token: " + token);
        // do we have a token?
        if (token == null) throw new BalsaSecurityException("Invalid token");
        // parse and verify the cookie
        try
        {
            CryptoCookie cookie = CryptoCookie.fromString(token);
            if (! this.securityEngine.getBaker().verify(cookie))
            {
                logger.error("Failed to verify authentication token");
                throw new BalsaSecurityException("Invalid token"); 
            }
            if (! cookie.isFlagSet(CryptoCookie.Flags.Principal))
            {
                logger.error("Failed to verify authentication token, incorrect flags");
                throw new BalsaSecurityException("Invalid token");
            }
            // convert the token to a principal
            Principal principal = this.securityEngine.principalForToken(cookie.getToken());
            // did we get a principal
            if (principal == null)
            {
                logger.error("No such principal could be found for token");
                throw new BalsaSecurityException("No such principal");
            }
            // validate
            this.securityEngine.validateAccessToken(token, cookie, principal, gt.getRequiredFlags());
            return new AuthenticatedPrincipal(principal, this.name);
        }
        catch (IOException e)
        {
            throw new BalsaSecurityException("Invalid token", e);
        }
    }

    @Override
    public AuthenticationChallenge generateAuthenticationChallenge(Principal principal) throws BalsaSecurityException
    {
        return null;
    }
}
