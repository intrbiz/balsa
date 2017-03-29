package com.intrbiz.balsa.engine.impl.security;

import static com.intrbiz.util.Hash.*;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.intrbiz.balsa.engine.SecurityEngine;
import com.intrbiz.balsa.engine.impl.AbstractBalsaEngine;
import com.intrbiz.balsa.engine.impl.security.method.PasswordAuthenticationMethod;
import com.intrbiz.balsa.engine.impl.security.method.TokenAuthenticationMethod;
import com.intrbiz.balsa.engine.security.AuthenticationResponse;
import com.intrbiz.balsa.engine.security.AuthenticationState;
import com.intrbiz.balsa.engine.security.PasswordSecurityEngine;
import com.intrbiz.balsa.engine.security.TokenSecurityEngine;
import com.intrbiz.balsa.engine.security.challenge.AuthenticationChallenge;
import com.intrbiz.balsa.engine.security.credentials.Credentials;
import com.intrbiz.balsa.engine.security.info.AuthenticationInfo;
import com.intrbiz.balsa.engine.security.info.SimpleAuthenticationInfo;
import com.intrbiz.balsa.engine.security.method.AuthenticatedPrincipal;
import com.intrbiz.balsa.engine.security.method.AuthenticationMethod;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.crypto.SecretKey;
import com.intrbiz.crypto.cookie.CookieBaker;
import com.intrbiz.crypto.cookie.CookieBaker.Expires;
import com.intrbiz.crypto.cookie.CryptoCookie;
import com.intrbiz.crypto.cookie.CryptoCookie.Flag;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;

public abstract class BaseSecurityEngine extends AbstractBalsaEngine implements SecurityEngine, TokenSecurityEngine, PasswordSecurityEngine
{   
    protected SecretKey applicationKey;
    
    protected int tokenLength;
    
    protected long lifetime;
    
    protected TimeUnit lifetimeUnit;
    
    protected int rebakeLimit;
    
    protected Flag[] flags;
    
    protected CookieBaker baker;
    
    private final Timer authenticateTimer;
    
    private final Counter singleFactorLogins;
    
    private final Counter primaryLogins;
    
    private final Counter secondaryLogins;
    
    private final Counter validLogins;
    
    private final Counter invalidLogins;
    
    private final Counter validVerifies;
    
    private final Counter invalidVerifies;
    
    private final ConcurrentMap<String, AuthenticationMethod<?>> authenticationMethods = new ConcurrentHashMap<String, AuthenticationMethod<?>>();
    
    public BaseSecurityEngine()
    {
        super();
        // setup metrics
        IntelligenceSource source = Witchcraft.get().source(this.getMetricsIntelligenceSourceName());
        this.authenticateTimer    = source.getRegistry().timer(Witchcraft.name(this.getClass(), "authenticate"));
        this.validLogins          = source.getRegistry().counter(Witchcraft.name(this.getClass(), "valid-logins"));
        this.invalidLogins        = source.getRegistry().counter(Witchcraft.name(this.getClass(), "invalid-logins"));
        this.singleFactorLogins   = source.getRegistry().counter(Witchcraft.name(this.getClass(), "single-factor-logins"));
        this.primaryLogins        = source.getRegistry().counter(Witchcraft.name(this.getClass(), "primary-logins"));
        this.secondaryLogins      = source.getRegistry().counter(Witchcraft.name(this.getClass(), "secondary-logins"));
        this.validVerifies        = source.getRegistry().counter(Witchcraft.name(this.getClass(), "valid-verifies"));
        this.invalidVerifies      = source.getRegistry().counter(Witchcraft.name(this.getClass(), "invalid-verifies"));
        // defaults
        this.applicationKey = SecretKey.generate();
        this.tokenLength = 32;
        this.lifetime = 1;
        this.lifetimeUnit = TimeUnit.HOURS;
        this.rebakeLimit = 24; // 1 day
        this.flags = new Flag[0];
        this.setupBaker();
        this.setupDefaultAuthenticationMethods();
    }
    
    /**
     * Setup the cookie baker used
     */
    protected void setupBaker()
    {
        this.baker = new CookieBaker(this.applicationKey, this.tokenLength, this.lifetime, this.lifetimeUnit, this.rebakeLimit, this.flags);    
    }
    
    /**
     * Setup our default authentication methods
     */
    protected void setupDefaultAuthenticationMethods()
    {
        this.registerAuthenticationMethod(new PasswordAuthenticationMethod());
        this.registerAuthenticationMethod(new TokenAuthenticationMethod());
    }

    @Override
    public Collection<AuthenticationMethod<?>> authenticationMethods()
    {
        return Collections.unmodifiableCollection(this.authenticationMethods.values());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends AuthenticationMethod<?>> T getAuthenticationMethod(String name)
    {
        return (T) this.authenticationMethods.get(name);
    }

    @Override
    public SecurityEngine registerAuthenticationMethod(AuthenticationMethod<?> method)
    {
        if (method != null)
        {
            this.authenticationMethods.put(method.name(), method);
            method.setup(this);
        }
        return this;
    }
    
    @Override
    public boolean isAuthenticationMethodRegistered(String name)
    {
        return this.authenticationMethods.containsKey(name);
    }
    
    protected void setupAuthenticationMethods()
    {
        for (AuthenticationMethod<?> authMeth : this.authenticationMethods.values())
        {
            authMeth.setup(this);
        }
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
    
    public Principal authenticateRequest(Credentials credentials, boolean forceSingleFactorAuthentication) throws BalsaSecurityException
    {
        AuthenticationResponse response = this.authenticate(null, credentials, forceSingleFactorAuthentication);
        if (response.isComplete()) 
            return response.getPrincipal();
        throw new BalsaSecurityException("Could not authenticate for this request only");
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationState state, Credentials credentials, boolean forceSingleFactorAuthentication) throws BalsaSecurityException
    {
        try (Timer.Context timerContext = this.authenticateTimer.time())
        {
            // execute the authentication method
            AuthenticatedPrincipal authedPrincipal = this.executeAutenticationMethodAuthenticate(state, credentials);
            if (authedPrincipal == null || authedPrincipal.getPrincipal() == null)
                throw new BalsaSecurityException("No such principal");
            // is two factor authentication enabled?
            if ((! forceSingleFactorAuthentication) && this.isTwoFactorAuthenticationRequiredForPrincipal(authedPrincipal.getPrincipal()))
            {
                // have we already authenticated this principal for the first time round
                Principal authenticating = state.authenticatingPrincipal();
                if (authenticating != null && authenticating.equals(authedPrincipal.getPrincipal()))
                {
                    // we've successfully applied the second factor
                    AuthenticationInfo info = state.info();
                    this.secondaryLogins.inc();
                    this.validLogins.inc();
                    return new AuthenticationResponse(authedPrincipal.getPrincipal(), new SimpleAuthenticationInfo(info == null ? null : info.primaryAuthenticationMethodName(), info == null ? null : info.primaryAuthenticationMethodDetail(), authedPrincipal.getAuthenticationMethod(), authedPrincipal.getAuthenticationInfoDetail()));
                }
                else
                {
                    // this is the first authentication attempt for this principal, generate any challenges we need
                    Map<String, AuthenticationChallenge> challenges = this.generateAuthenticationChallenges(authedPrincipal.getPrincipal());
                    this.primaryLogins.inc();
                    return new AuthenticationResponse(authedPrincipal.getPrincipal(), false, new SimpleAuthenticationInfo(authedPrincipal.getAuthenticationMethod(), authedPrincipal.getAuthenticationInfoDetail(), null, null), challenges);
                }
            }
            else
            {
                this.singleFactorLogins.inc();
                this.validLogins.inc();
                return new AuthenticationResponse(authedPrincipal.getPrincipal(), new SimpleAuthenticationInfo(authedPrincipal.getAuthenticationMethod(), authedPrincipal.getAuthenticationInfoDetail(), null, null));
            }
        }
        catch (BalsaSecurityException e)
        {
            this.invalidLogins.inc();
            throw e;
        }
    }
    
    public void verify(AuthenticationState state, Credentials credentials) throws BalsaSecurityException
    {
        try
        {
            // execute the authentication method
            this.executeAutenticationMethodVerify(state, credentials);
            this.validVerifies.inc();
        }
        catch (BalsaSecurityException e)
        {
            this.invalidVerifies.inc();
            throw e;
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void executeAutenticationMethodVerify(AuthenticationState state, Credentials credentials) throws BalsaSecurityException
    {
        for (AuthenticationMethod<?> authMeth : this.authenticationMethods.values())
        {
            if (authMeth.isValidFor(credentials))
            {
                ((AuthenticationMethod) authMeth).verify(state, credentials);
                return;
            }
        }
        throw new BalsaSecurityException("No authentication method could be found");
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected AuthenticatedPrincipal executeAutenticationMethodAuthenticate(AuthenticationState state, Credentials credentials) throws BalsaSecurityException
    {
        for (AuthenticationMethod<?> authMeth : this.authenticationMethods.values())
        {
            if (authMeth.isValidFor(credentials))
            {
                return ((AuthenticationMethod) authMeth).authenticate(state, credentials);
            }
        }
        throw new BalsaSecurityException("No authentication method could be found");
    }
    
    @Override
    public Map<String, AuthenticationChallenge> generateAuthenticationChallenges(Principal principal) throws BalsaSecurityException
    {
        Map<String, AuthenticationChallenge> challenges = new HashMap<String, AuthenticationChallenge>();
        for (AuthenticationMethod<?> authMeth : this.authenticationMethods.values())
        {
            AuthenticationChallenge challenge = authMeth.generateAuthenticationChallenge(principal);
            if (challenge != null)
                challenges.put(authMeth.name(), challenge);
        }
        return challenges;
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
            this.setupAuthenticationMethods();
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
    
    /**
     * Validate the given access token. By default perpetual access tokens are rejected.
     * 
     * @param token the original string token passed by the application to authenticate with
     * @param cookie the parsed CryptoCookie value of the token, containing information such as the expiry time
     * @param principal the Principal that is represented by the token
     * @param requiredFlags the flags which must be set on the cookie as requested by the application
     * @throws BalsaSecurityException if the token is considered invalid
     */
    public void validateAccessToken(String token, CryptoCookie cookie, Principal principal, CryptoCookie.Flag[] requiredFlags) throws BalsaSecurityException
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
    
    @Override
    public CookieBaker getBaker()
    {
        return this.baker;
    }
    
    // implementation specific internal methods
    
    /**
     * The name of the Witchcraft Intelligence Source metrics will be registered under.
     */
    protected String getMetricsIntelligenceSourceName()
    {
        return "com.intrbiz.balsa";
    }
}
