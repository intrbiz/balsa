package com.intrbiz.balsa.engine.route;

import static com.intrbiz.balsa.BalsaContext.*;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.SecurityEngine.ValidationLevel;
import com.intrbiz.balsa.engine.security.Credentials;
import com.intrbiz.balsa.error.BalsaSecurityException;
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
    protected final <T> T model(String name, Class<T> type, boolean create)
    {
        return Balsa().model(name, type, create);
    }
    
    protected final <T> T model(String name, T model)
    {
        return Balsa().model(name, model);
    }
    
    /**
     * Get the named variable
     * @param name the variable name
     * @return
     * returns Object
     */
    public <T> T var(String name)
    {
        return Balsa().var(name);
    }
    
    /**
     * Store a variable
     * @param name the variable name
     * @param object the variable
     * returns void
     */
    public <T> T var(String name, T object)
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
    public List<String> listParam(String name)
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
    public String url(String url)
    {
        return Balsa().url(url);
    }

    /**
     * Translate the given path to a server absolute path using information from the request.
     * @param path the path to make absolute
     * @return
     * returns String
     */
    public String path(String path)
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
    public String pub(String path)
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
     * Authenticate for the life of this session, this method 
     * will always return with a valid, authenticated user.
     * @throws BalsaSecurityException should there be any issues authenticating the user.
     */
    protected <T extends Principal> T authenticate(Credentials credentials)
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
    protected <T extends Principal> T tryAuthenticate(Credentials credentials)
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
     * Get the named session variable
     * @param name the variable name
     * @return
     * returns Object
     */
    public <T> T sessionVar(String name)
    {
        return Balsa().sessionVar(name);
    }
    
    /**
     * Store a variable in the session
     * @param name the variable name
     * @param object the variable
     * returns void
     */
    public <T> T sessionVar(String name, T object)
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
    protected final <T> T sessionModel(String name, Class<T> type, boolean create)
    {
        return Balsa().sessionModel(name, type, create);
    }
    
    protected final <T> T sessionModel(String name, T model)
    {
        return Balsa().sessionModel(name, model);
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
    
}
