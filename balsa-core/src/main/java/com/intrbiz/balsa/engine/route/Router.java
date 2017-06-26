package com.intrbiz.balsa.engine.route;

import static com.intrbiz.balsa.BalsaContext.*;

import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.SecurityEngine.ValidationLevel;
import com.intrbiz.balsa.engine.security.AuthenticationResponse;
import com.intrbiz.balsa.engine.security.AuthenticationState;
import com.intrbiz.balsa.engine.security.credentials.Credentials;
import com.intrbiz.balsa.engine.security.info.AuthenticationInfo;
import com.intrbiz.balsa.engine.task.BalsaTaskState;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.listener.BalsaRequest;
import com.intrbiz.balsa.listener.BalsaResponse;
import com.intrbiz.balsa.util.CookieBuilder;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Template;

/**
 * A router routes requests
 */
public abstract class Router<A extends BalsaApplication>
{   
    private final String prefix = this.myPrefix();
    
    private final String[] templates = this.myTemplates();
    
    private final String myPrefix()
    {
        Class<?> clazz = this.getClass();
        Prefix prefix = clazz.getAnnotation(Prefix.class);
        if (prefix == null) return "/";
        return prefix.value();
    }
    
    private final String[] myTemplates()
    {
        Class<?> clazz = this.getClass();
        Template template = clazz.getAnnotation(Template.class);
        if (template == null || template.value() == null) return new String[0];
        return template.value();
    }
    
    public final String prefix()
    {
        return this.prefix;
    }
    
    public final String getPrefix()
    {
        return this.prefix;
    }
    
    public final String[] templates()
    {
        return this.templates;
    }
    
    public final String[] getTemplates()
    {
        return this.templates;
    }
    
    /*
     * Router mandatory methods
     */

    /**
     * Do some stuff before we process this router
     * 
     * returns void
     */
    public void before() throws Exception
    {
    }

    /**
     * Do some stuff after we have processed this router
     * 
     * returns void
     */
    public void after() throws Exception
    {
    }

    /*
     * Helper methods
     */

    /**
     * Create the request bean of the given name
     * 
     * @param name
     *            the bean name
     * @param type
     *            the bean class
     * @return returns Object the bean
     */    
    protected final <T> T createModel(String name, Class<T> type)
    {
        return Balsa().createModel(name, type);
    }
    
    protected final <T> T model(String name, T model)
    {
        return Balsa().model(name, model);
    }
    
    protected final <T> T model(String name)
    {
        return Balsa().model(name);
    }
    
    /**
     * Get the named variable
     * @param name the variable name
     * @return
     * returns Object
     */
    protected final <T> T var(String name)
    {
        return Balsa().var(name);
    }
    
    /**
     * Store a variable
     * @param name the variable name
     * @param object the variable
     * returns void
     */
    protected final <T> T var(String name, T object)
    {
        return Balsa().var(name, object);
    }

    /**
     * Get the value of the request parameter given
     * 
     * @param name
     *            the parameter name
     * @return returns String the parameter value
     */
    protected final String param(String name)
    {
        return Balsa().param(name);
    }
    
    /**
     * Get the value of the request parameter given
     * @param name the parameter name
     * @return the List&lt;String&gt; parameter value or null
     */
    protected final List<String> listParam(String name)
    {
        return Balsa().listParam(name);
    }

    /**
     * Decode the given views
     * 
     * @param views
     *            returns void
     */
    protected final void decode(String... views) throws BalsaException
    {
        Balsa().decode(new String[][]{ this.app().templates(), this.templates() }, views);
    }
    
    /**
     * Decode the given views without using any templates
     * @param views
     * @throws BalsaException
     */
    protected final void decodeOnly(String... views) throws BalsaException
    {
        Balsa().decode(null, views);
    }

    /**
     * Respond by encoding the given views
     * 
     * NB: This will not set the content type and status of the response.
     * @param views  the views to encode
     * @throws BalsaException
     * returns void
     */
    protected final void encode(String... views) throws BalsaException
    {
        Balsa().encode(new String[][]{ this.app().templates(), this.templates() }, views);
    }
    
    protected final String encodeBuffered(String... views) throws BalsaException
    {
        return Balsa().encodeBuffered(new String[][]{ this.app().templates(), this.templates() }, views);
    }

    /**
     * Respond by encoding the given views, without using any templates.
     * 
     * NB: This will not set the content type and status of the response.
     * @param views  the views to encode
     * @throws BalsaException
     * returns void
     */
    protected final void encodeOnly(String... views) throws BalsaException
    {
        Balsa().encodeOnly(views);
    }
    
    protected final String encodeOnlyBuffered(String... views) throws BalsaException
    {
        return Balsa().encodeOnlyBuffered(views);
    }
    
    /**
     * Translate the given URL into an absolute URL using information from the request.
     * 
     * The URL will not be translated, and returned unchanged, if:
     *  1. the URL has a protocol (start with: '[A-Za-z]://')
     *  2. the URL starts with '//'
     * 
     * @param url  the relative url to translate
     * @return
     * returns String
     */
    protected final String url(String url)
    {
        return Balsa().url(url);
    }

    /**
     * Translate the given path to a server absolute path using information from the request.
     * @param path the path to make absolute
     * @return
     * returns String
     */
    protected final String path(String path)
    {
        return Balsa().path(path);
    }
    
    /**
     * Translate the given relative path into the URL for the public resource.
     * 
     * The resulting URL maybe an absolute server path or an absolute URL.
     * 
     * 
     * 
     * @param path the relative path to the public resource
     * @return the URL to the resource.
     */
    protected final String pub(String path)
    {
        return Balsa().pub(path);
    }

    /**
     * Redirect to another URL
     * 
     * @param url
     *            the URL to redirect to
     * @param permanent
     *            it the redirect permenant (301) returns void
     */
    protected final void redirect(String url, boolean permanent) throws IOException
    {
        Balsa().redirect(url, permanent);
    }

    /**
     * Redirect (302) to another URL
     * 
     * @param url
     *            the URL to redirect to returns void
     */
    protected final void redirect(String url) throws IOException
    {
        Balsa().redirect(url);
    }

    /**
     * Require a security constraint to be met
     * 
     * @param constraint
     *            returns void
     */
    protected final void require(boolean constraint) throws BalsaException
    {
        Balsa().require(constraint);
    }

    protected final void require(boolean constraint, String message) throws BalsaException
    {
        Balsa().require(constraint, message);
    }

    protected final <E extends Exception> void require(boolean constraint, E securityException) throws E
    {
        Balsa().require(constraint, securityException);
    }

    /**
     * Check that the current user is valid
     */
    protected final boolean validPrincipal(ValidationLevel validationLevel)
    {
        return Balsa().validPrincipal(validationLevel);
    }
    
    /**
     * Check that the current user is valid (strongly)
     */
    protected final boolean validPrincipal()
    {
        return Balsa().validPrincipal();
    }
    
    /**
     * Check that the current user is valid (weakly)
     */
    protected final boolean principal()
    {
        return Balsa().principal();
    }
    
    /**
     * Get the authentication state for this current session
     */
    protected final AuthenticationState authenticationState()
    {
        return Balsa().authenticationState();
    }
    
    /**
     * Get the authentication info for this current session
     */
    protected final AuthenticationInfo authenticationInfo()
    {
        return Balsa().authenticationInfo();
    }
    
    @SuppressWarnings("unchecked")
    protected final <T extends Principal> T currentPrincipal()
    {
        return (T) Balsa().currentPrincipal();
    }
    
    protected void deauthenticate()
    {
        Balsa().deauthenticate();
    }
    
    /**
     * Authenticate for the life of this session, this method 
     * will always return with a valid, authenticated user.
     * @throws BalsaSecurityException should there be any issues authenticating the user.
     */
    protected <T extends Principal> T authenticate(String username, String password)
    {
        return Balsa().authenticate(username, password);
    }
    
    /**
     * Authenticate for the life of this session, using a single factor authentication. 
     * This method will always return with a valid, authenticated user.
     * @throws BalsaSecurityException should there be any issues authenticating the user.
     */
    protected <T extends Principal> T authenticateSingleFactor(Credentials credentials, boolean force)
    {
        return Balsa().authenticateSingleFactor(credentials, force);
    }
    
    /**
     * Start the authentication process.  The response will specify if
     * @throws BalsaSecurityException should there be any issues authenticating the user.
     */
    protected final AuthenticationResponse authenticate(Credentials credentials) throws BalsaSecurityException
    {
        return Balsa().authenticate(credentials);
    }
    
    /**
     * Authenticate for the life of this request only, this avoids creating a session, 
     * this method will always return with a valid, authenticated user.
     * @throws BalsaSecurityException should there be any issues authenticating the user.
     */
    protected <T extends Principal> T authenticateRequest(String username, String password)
    {
        return Balsa().authenticateRequest(username, password);
    }
    
    /**
     * Authenticate for the life of this request only, this avoids creating a session, 
     * this method will always return with a valid, authenticated user.
     * @throws BalsaSecurityException should there be any issues authenticating the user.
     */
    protected <T extends Principal> T authenticateRequest(Credentials credentials)
    {
        return Balsa().authenticateRequest(credentials);
    }
    
    /**
     * Authenticate for the life of this request only using a single authentication factor, 
     * this avoids creating a session, this method will always return with a valid, authenticated user.
     * @throws BalsaSecurityException should there be any issues authenticating the user.
     */
    protected <T extends Principal> T authenticateRequestSingleFactor(Credentials credentials)
    {
        return Balsa().authenticateRequestSingleFactor(credentials);
    }
    
    /**
     * Try to authenticate for the life of this session, should 
     * authentication not be possible then null is returned, exceptions are thrown.
     */
    protected <T extends Principal> T tryAuthenticate(String username, String password)
    {
        return Balsa().tryAuthenticate(username, password);
    }
    
    /**
     * Try to authenticate for the life of this session, should 
     * authentication not be possible then null is returned, exceptions are thrown.
     */
    protected <T extends Principal> T tryAuthenticateSingleFactor(Credentials credentials, boolean force)
    {
        return Balsa().tryAuthenticateSingleFactor(credentials, force);
    }
    
    /**
     * Try to authenticate for the life of this session, should 
     * authentication not be possible then null is returned, exceptions are thrown.
     */
    protected AuthenticationResponse tryAuthenticate(Credentials credentials)
    {
        return Balsa().tryAuthenticate(credentials);
    }
    
    /**
     * Try to authenticate for the life of this request only, this avoids creating a session, 
     * should authentication not be possible then null is returned, exceptions are thrown.
     */
    protected <T extends Principal> T tryAuthenticateRequest(String username, String password)
    {
        return Balsa().tryAuthenticateRequest(username, password);
    }
    
    /**
     * Try to authenticate for the life of this request only, this avoids creating a session, 
     * should authentication not be possible then null is returned, exceptions are thrown.
     */
    protected <T extends Principal> T tryAuthenticateRequest(Credentials credentials)
    {
        return Balsa().tryAuthenticateRequest(credentials);
    }

    /**
     * Check that the current user has the given permission
     * 
     * @param permission
     *            the permission name
     * @return returns boolean
     */
    protected final boolean permission(String permission)
    {
        return Balsa().permission(permission);
    }
    
    /**
     * Check that the current user has the given permission over the given object
     * @param permission the permission name
     * @param object the object over which permission must be granted
     * @return true if and only if the current user has the given permission over th given object
     */
    protected final boolean permission(String permission, Object object)
    {
        return Balsa().permission(permission, object);
    }
    
    /**
     * Filter the given collection returning only the objects which the current 
     * user has the given permission over
     * @param permission
     * @param objects
     * @return
     */
    protected final <T> List<T> permission(String permission, Collection<T> objects)
    {
        List<T> ret = new LinkedList<T>();
        for (T object : objects)
        {
            if (permission(permission, object)) ret.add(object);
        }
        return ret;
    }
    
    /**
     * Filter the given collection returning only the objects which the current 
     * user has the given permission over
     * @param permission
     * @param objects
     * @return
     */
    protected final <T> Set<T> permission(String permission, Set<T> objects)
    {
        Set<T> ret = new HashSet<T>();
        for (T object : objects)
        {
            if (permission(permission, object)) ret.add(object);
        }
        return ret;
    }
    
    /**
     * No authentication is currently happening or has not happened
     */
    protected final boolean notAuthenticated()
    {
        return Balsa().notAuthenticated();
    }
    
    /**
     * Authentication is currently in progress
     */
    protected final boolean authenticating()
    {
        return Balsa().authenticating();
    }
    
    /**
     * Do we have an authenticated principal
     */
    protected final boolean authenticated()
    {
        return Balsa().authenticated();
    }
    
    /**
     * Get the named session variable
     * @param name the variable name
     * @return
     * returns Object
     */
    protected final <T> T sessionVar(String name)
    {
        return Balsa().sessionVar(name);
    }
    
    /**
     * Store a variable in the session
     * @param name the variable name
     * @param object the variable
     * returns void
     */
    protected final <T> T sessionVar(String name, T object)
    {
        return Balsa().sessionVar(name, object);
    }
    
    /**
     * Create the session model of the given name
     * 
     * @param name
     *            the model name
     * @param type
     *            the model class
     * @return returns Object the model
     */
    protected final <T> T createSessionModel(String name, Class<T> type)
    {
        return Balsa().createSessionModel(name, type);
    }

    /**
     * Put a model into the session and our local session model cache
     * @param name the model name
     * @param model the model
     * @return the model
     */
    protected final <T> T sessionModel(String name, T model)
    {
        return Balsa().sessionModel(name, model);
    }
    
    /**
     * Get a model from the session and promote it to our local session model cache
     * @param name the model name
     * @return the model or null
     */
    protected final <T> T sessionModel(String name)
    {
        return Balsa().sessionModel(name);
    }
    
    /**
     * Try to get a session model without forcefully allocating a session
     * @param name the model name
     * @return the model
     */
    protected final <T> T trySessionModel(String name)
    {
        return Balsa().trySessionModel(name);
    }
    
    /**
     * Promote all cached session models to the session
     */
    protected final void promoteSessionModelCache()
    {
        Balsa().promoteSessionModelCache();
    }
    
    /**
     * Remove all cached session models
     */
    protected final void clearSessionModelCache()
    {
        Balsa().clearSessionModelCache();
    }
    
    /**
     * Forcefully remove the session model from our local cache
     * @param name the model name
     */
    protected final void uncacheSessionModel(String name)
    {
        Balsa().uncacheSessionModel(name);
    }
    
    protected final <T> T action(String name, Object... arguments) throws BalsaException
    {
        try
        {
            return Balsa().action(name, arguments);
        }
        catch (Exception e)
        {
            throw new BalsaException("Failed to perform action: " + name, e);
        }
    }
    
    protected final String deferredAction(String action, Object... arguments)
    {
        return Balsa().deferredAction(action, arguments);
    }
    
    protected final String deferredActionWithId(String id, String action, Object... arguments)
    {
        return Balsa().deferredActionWithId(id, action, arguments);
    }
    
    protected final BalsaTaskState pollDeferredAction(String id)
    {
        return Balsa().pollDeferredAction(id);
    }
    
    /**
     * Get the current Balsa context
     * @return
     */
    protected final BalsaContext balsa()
    {
        return Balsa();
    }
    
    /**
     * Get the current Balsa request
     * @return
     */
    protected final BalsaRequest request()
    {
        return Balsa().request();
    }
    
    /**
     * Get the value of the given header
     * @param name the header name
     * @return
     */
    protected final String header(String name)
    {
        return Balsa().header(name);
    }
    
    /**
     * Get the value of the given cookie
     * @param name the cookie name
     * @return
     */
    protected final String cookie(String name)
    {
        return Balsa().cookie(name);
    }
    
    /**
     * Set a cookie.  Cookies are set using a fluent interface, 
     * for example: <code>cookie().name("name").value("value").set()</code>
     * @return A CookieBuilder to create and set the cookie.
     */
    public CookieBuilder<BalsaResponse> cookie()
    {
        return Balsa().cookie();
    }
    
    /**
     * Get the current Balsa response
     * @return
     */
    protected final BalsaResponse response()
    {
        return Balsa().response();
    }
    
    protected final A app()
    {
       return Balsa().app(); 
    }
    
    protected <T> T notNull(T o) throws BalsaNotFound
    {
        if (o == null) throw new BalsaNotFound();
        return o;
    }
    
    protected <T> T notNull(T o, String message) throws BalsaNotFound
    {
        if (o == null) throw new BalsaNotFound(message);
        return o;
    }
    
    // Lifecycle
    
    /**
     * Setup this router
     */
    public void setup() throws Exception
    { 
    }
    
    /**
     * Start this router
     */
    public void start() throws Exception
    {
    }
}
