package com.intrbiz.balsa.engine.impl.security.method;

import java.security.Principal;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.SecurityEngine;
import com.intrbiz.balsa.engine.security.AuthenticationState;
import com.intrbiz.balsa.engine.security.PasswordSecurityEngine;
import com.intrbiz.balsa.engine.security.challenge.AuthenticationChallenge;
import com.intrbiz.balsa.engine.security.credentials.Credentials;
import com.intrbiz.balsa.engine.security.credentials.PasswordCredentials;
import com.intrbiz.balsa.engine.security.method.AuthenticatedPrincipal;
import com.intrbiz.balsa.engine.security.method.AuthenticationMethod;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.balsa.util.Util;

/**
 * Provide password based authentication.
 * 
 * Note: this authentication provided must be registered 
 * against a security engine which implements SecurityEngine.PasswordSecurityEngine
 */
public class PasswordAuthenticationMethod extends BaseAuthenticationMethod<PasswordCredentials>
{
    private static Logger logger = Logger.getLogger(PasswordAuthenticationMethod.class);
    
    protected PasswordSecurityEngine securityEngine;
    
    public PasswordAuthenticationMethod()
    {
        super(PasswordCredentials.class, AuthenticationMethod.PASSWORD);
    }
    
    @Override
    public void setup(SecurityEngine engine) throws BalsaException
    {
        if (! (engine instanceof PasswordSecurityEngine))
            throw new BalsaException("The password authentication method can only be registered against a security engine which implements SecurityEngine.PasswordSecurityEngine");
        this.securityEngine = (PasswordSecurityEngine) engine;
    }

    @Override
    public boolean isValidFor(Credentials credentials)
    {
        return credentials instanceof PasswordCredentials;
    }

    @Override
    public AuthenticatedPrincipal authenticate(AuthenticationState state, PasswordCredentials pw) throws BalsaSecurityException
    {
        logger.debug("Authentication for principal: " + pw.getPrincipalName());
        if (Util.isEmpty(pw.getPrincipalName()))
        {
            throw new BalsaSecurityException("Username not provided");
        }
        if (pw.getPassword() == null || pw.getPassword().length == 0)
        {
            throw new BalsaSecurityException("Password not provided");
        }
        // do the login
        Principal principal = this.securityEngine.doPasswordLogin(pw.getPrincipalName(), pw.getPassword());
        // did we get a principal
        if (principal == null)
        {
            logger.error("No such principal " + pw.getPrincipalName() + " could be found.");
            throw new BalsaSecurityException("No such principal");
        }
        return new AuthenticatedPrincipal(principal, this.name);
    }

    @Override
    public AuthenticationChallenge generateAuthenticationChallenge(Principal principal) throws BalsaSecurityException
    {
        return null;
    }
}
