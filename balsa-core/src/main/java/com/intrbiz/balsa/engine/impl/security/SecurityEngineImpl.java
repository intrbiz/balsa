package com.intrbiz.balsa.engine.impl.security;

import static com.intrbiz.util.Hash.*;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.intrbiz.balsa.engine.SecurityEngine;
import com.intrbiz.balsa.engine.impl.AbstractBalsaEngine;
import com.intrbiz.balsa.engine.security.Credentials;
import com.intrbiz.balsa.engine.security.GenericAuthenticationToken;
import com.intrbiz.balsa.engine.security.PasswordCredentials;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.balsa.util.Util;
import com.intrbiz.crypto.SecretKey;
import com.intrbiz.crypto.cookie.CookieBaker;
import com.intrbiz.crypto.cookie.CookieBaker.Expires;
import com.intrbiz.crypto.cookie.CryptoCookie;
import com.intrbiz.crypto.cookie.CryptoCookie.Flag;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;

public class SecurityEngineImpl extends AbstractBalsaEngine implements SecurityEngine
{
    private Logger logger = Logger.getLogger(SecurityEngineImpl.class);
    
    protected SecretKey applicationKey;
    
    protected int tokenLength;
    
    protected long lifetime;
    
    protected TimeUnit lifetimeUnit;
    
    protected int rebakeLimit;
    
    protected Flag[] flags;
    
    protected CookieBaker baker;
    
    private final Timer authenticateTimer;
    
    private final Counter validLogins;
    
    private final Counter invalidLogins;
    
    public SecurityEngineImpl()
    {
        super();
        // setup metrics
        IntelligenceSource source = Witchcraft.get().source(this.getMetricsIntelligenceSourceName());
        this.authenticateTimer = source.getRegistry().timer(Witchcraft.name(this.getClass(), "authenticate"));
        this.validLogins       = source.getRegistry().counter(Witchcraft.name(this.getClass(), "valid-logins"));
        this.invalidLogins     = source.getRegistry().counter(Witchcraft.name(this.getClass(), "invalid-logins"));
        // defaults
        this.applicationKey = SecretKey.generate();
        this.tokenLength = 32;
        this.lifetime = 1;
        this.lifetimeUnit = TimeUnit.HOURS;
        this.rebakeLimit = 24; // 1 day
        this.flags = new Flag[0];
        this.setupBaker();
    }
    
    protected final void setupBaker()
    {
        this.baker = new CookieBaker(this.applicationKey, this.tokenLength, this.lifetime, this.lifetimeUnit, this.rebakeLimit, this.flags);
    }

    @Override
    public String getEngineName()
    {
        return "Balsa-Security-Engine";
    }
    
    @Override
    public Principal defaultPrincipal()
    {
        return null;
    }

    @Override
    public Principal authenticate(Credentials credentials) throws BalsaSecurityException
    {
        final Timer.Context tCtx = this.authenticateTimer.time();
        try
        {
            if (credentials instanceof PasswordCredentials)
            {
                PasswordCredentials pw = (PasswordCredentials) credentials;
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
                Principal principal = this.doPasswordLogin(pw.getPrincipalName(), pw.getPassword());
                // did we get a principal
                if (principal == null)
                {
                    logger.error("No such principal " + pw.getPrincipalName() + " could be found.");
                    throw new BalsaSecurityException("No such principal");
                }
                // all good
                this.validLogins.inc();
                return principal;
            }
            else if (credentials instanceof GenericAuthenticationToken)
            {
                String token = ((GenericAuthenticationToken) credentials).getToken();
                logger.debug("Authenticating token: " + token);
                // do we have a token?
                if (token == null)
                {
                    throw new BalsaSecurityException("Invalid token");
                }
                // parse and verify the cookie
                try
                {
                    CryptoCookie cookie = CryptoCookie.fromString(token);
                    if (!this.baker.verify(cookie))
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
                    Principal principal = this.principalForToken(cookie.getToken());
                    // did we get a principal
                    if (principal == null)
                    {
                        logger.error("No such principal could be found for token");
                        throw new BalsaSecurityException("No such principal");
                    }
                    // validate
                    this.validateAccessToken(token, cookie, principal, ((GenericAuthenticationToken) credentials).getRequiredFlags());
                    // all good
                    this.validLogins.inc();
                    return principal;
                }
                catch (IOException e)
                {
                    throw new BalsaSecurityException("Invalid token", e);
                }
            }
            throw new BalsaSecurityException("No such principal");
        }
        catch (BalsaSecurityException e)
        {
            this.invalidLogins.inc();
            throw e;
        }
        finally
        {
            tCtx.stop();
        }
    }

    @Override
    public boolean check(Principal principal, String permission)
    {
        return false;
    }
    
    @Override
    public boolean check(Principal principal, String permission, Object object)
    {
        return false;
    }

    @Override
    public boolean isValidPrincipal(Principal principal, ValidationLevel validationLevel)
    {
        return principal != null;
    }

    @Override
    public void applicationKey(SecretKey key)
    {
        if (key != null)
        {
            this.applicationKey = key;
            this.setupBaker();
        }
    }

    @Override
    public SecretKey getApplicationKey()
    {
        return this.applicationKey;
    }

    @Override
    public String generateAccessToken()
    {
        return this.baker.bake(CryptoCookie.Flags.AntiForgery).toString();
    }

    @Override
    public String generateAccessToken(long expiresAt)
    {
        return this.baker.bake(expiresAt, CryptoCookie.Flags.AntiForgery).toString();
    }

    @Override
    public String generateAccessTokenForURL(String url)
    {
        return this.baker.bake(sha256(asUTF8(url)), CryptoCookie.Flags.AntiForgery).toString();
    }

    @Override
    public String generateAccessTokenForURL(String url, long expiresAt)
    {
        return this.baker.bake(sha256(asUTF8(url)), expiresAt, CryptoCookie.Flags.AntiForgery).toString();
    }
    
    @Override
    public String generateAuthenticationTokenForPrincipal(Principal principal, CryptoCookie.Flag... flags)
    {
        byte[] token = this.tokenForPrincipal(principal);
        if (token == null || token.length == 0) throw new BalsaSecurityException("Cannot generate authentication token for principal");
        return this.baker.bake(token, CryptoCookie.flags(flags, CryptoCookie.Flags.Principal)).toString();
    }
    
    @Override
    public String generateAuthenticationTokenForPrincipal(Principal principal, long expiresAt, CryptoCookie.Flag... flags)
    {
        byte[] token = this.tokenForPrincipal(principal);
        if (token == null || token.length == 0) throw new BalsaSecurityException("Cannot generate authentication token for principal");
        return this.baker.bake(token, expiresAt, CryptoCookie.flags(flags, CryptoCookie.Flags.Principal)).toString();
    }
    
    @Override
    public String generatePerpetualAuthenticationTokenForPrincipal(Principal principal, CryptoCookie.Flag... flags)
    {
        byte[] token = this.tokenForPrincipal(principal);
        if (token == null || token.length == 0) throw new BalsaSecurityException("Cannot generate authentication token for principal");
        return this.baker.bake(token, Expires.never(), CryptoCookie.flags(flags, CryptoCookie.Flags.Perpetual, CryptoCookie.Flags.Principal)).toString();
    }
    
    @Override
    public String regenerateAuthenticationTokenForPrincipal(String token)
    {
        try
        {
            CryptoCookie cookie = CryptoCookie.fromString(token);
            CryptoCookie rebaked = this.baker.rebake(cookie);
            if (rebaked == null) throw new BalsaSecurityException("Cannot regenerate authentication token");
            return rebaked.toString();
        }
        catch (IOException e)
        {
            throw new BalsaSecurityException("Cannot regenerate authentication token, the given token is malformed", e);
        }
    }
    
    @Override
    public String regenerateAuthenticationTokenForPrincipal(String token, long expiresAt)
    {
        try
        {
            CryptoCookie cookie = CryptoCookie.fromString(token);
            CryptoCookie rebaked = this.baker.rebake(cookie, expiresAt);
            if (rebaked == null) throw new BalsaSecurityException("Cannot regenerate authentication token");
            return rebaked.toString();
        }
        catch (IOException e)
        {
            throw new BalsaSecurityException("Cannot regenerate authentication token, the given token is malformed", e);
        }
    }

    @Override
    public boolean validAccess(String token)
    {
        // parse and verify the cookie
        try
        {
            CryptoCookie cookie = CryptoCookie.fromString(token);
            return this.baker.verify(cookie) && cookie.isFlagSet(CryptoCookie.Flags.AntiForgery);
        }
        catch (IOException e)
        {
        }
        return false;
    }

    @Override
    public boolean validAccessForURL(String url, String token)
    {
        // parse and verify the cookie
        try
        {
            CryptoCookie cookie = CryptoCookie.fromString(token);
            if (!this.baker.verify(cookie)) return false;
            if (! cookie.isFlagSet(CryptoCookie.Flags.AntiForgery)) return false;
            // verify the URL
            return Arrays.equals(cookie.getToken(), sha256(asUTF8(url))); 
        }
        catch (IOException e)
        {
        }
        return false;
    }
    
    // implementation specific internal methods
    
    /**
     * The name of the Witchcraft Intelligence Source metrics will be registered under.
     */
    protected String getMetricsIntelligenceSourceName()
    {
        return "com.intrbiz.balsa";
    }
    
    /**
     * Map the given Principal to a token
     */
    protected byte[] tokenForPrincipal(Principal principal)
    {
        return null;
    }

    /**
     * Map the given token to a Principal
     */
    protected Principal principalForToken(byte[] token)
    {
        return null;
    }
    
    /**
     * Perform password based authentication for the given username and password 
     * returning the corresponding Principal.
     * @param username the username of the principal
     * @param password the password of the principal
     * @return the Principal or null
     */
    protected Principal doPasswordLogin(String username, char[] password) throws BalsaSecurityException
    {
        return null;
    }
    
    /**
     * Validate the given access token. By default perpetual access tokens are rejected, 
     * 
     * @param token the original string token passed by the application to authenticate with
     * @param cookie the parsed CryptoCookie value of the token, containing information such as the expiry time
     * @param principal the Principal that is represented by the token
     * @param requiredFlags the flags which must be set on the cookie as requested by the application
     * @throws BalsaSecurityException if the token is considered invalid
     */
    protected void validateAccessToken(String token, CryptoCookie cookie, Principal principal, CryptoCookie.Flag[] requiredFlags) throws BalsaSecurityException
    {
        // require a non-perpetual token
        if (cookie.getExpiryTime() == Expires.never())
        {
            throw new BalsaSecurityException("Perpetual access tokens are not permitted");
        }
        // validate the flags
        if (requiredFlags != null)
        {
            for (CryptoCookie.Flag flag : requiredFlags)
            {
                if (! cookie.isFlagSet(flag)) throw new BalsaSecurityException("The flag: " + flag.mask + " is missing from the access token");
            }
        }
    }
}
