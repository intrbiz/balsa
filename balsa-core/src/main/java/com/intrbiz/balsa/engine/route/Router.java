package com.intrbiz.balsa.engine.route;

import static com.intrbiz.balsa.BalsaContext.Balsa;

import java.io.IOException;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.scgi.SCGIResponse.Status;

/**
 * A router routes requests
 */
public abstract class Router
{
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
    protected <T> T model(String name, Class<T> type)
    {
        return Balsa().model(name, type);
    }
    
    protected <T> T model(String name, Class<T> type, boolean create)
    {
        return Balsa().model(name, type, create);
    }
    
    protected <T> T model(String name, T model)
    {
        return Balsa().model(name, model);
    }

    /**
     * Get the value of the request parameter given
     * 
     * @param name
     *            the parameter name
     * @return returns String the parameter value
     */
    protected String param(String name)
    {
        return Balsa().param(name);
    }

    /**
     * Decode the given views
     * 
     * @param views
     *            returns void
     */
    protected void decode(String... views) throws BalsaException
    {
        Balsa().decode(views);
    }

    /**
     * Respond by encoding the given views
     * 
     * NB: This will not set the content type and status of the response.
     * @param views  the views to encode
     * @throws BalsaException
     * returns void
     */
    protected void encode(String... views) throws BalsaException
    {
        Balsa().encode(views);
    }
    
    /**
     * Set the response content type and status and encode the given views.  
     * @param contentType the content type of the response
     * @param status      the status of the response
     * @param views       the views to encode
     * @throws BalsaException
     * returns void
     */
    public void encode(String contentType, Status status, String... views) throws BalsaException
    {
        Balsa().encode(contentType, status, views);
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
     * Redirect to another URL
     * 
     * @param url
     *            the URL to redirect to
     * @param permanent
     *            it the redirect permenant (301) returns void
     */
    protected void redirect(String url, boolean permanent) throws IOException
    {
        Balsa().redirect(url, permanent);
    }

    /**
     * Redirect (302) to another URL
     * 
     * @param url
     *            the URL to redirect to returns void
     */
    protected void redirect(String url) throws IOException
    {
        Balsa().redirect(url);
    }

    /**
     * Require a security constraint to be met
     * 
     * @param constraint
     *            returns void
     */
    protected void require(boolean constraint) throws BalsaException
    {
        Balsa().require(constraint);
    }

    protected void require(boolean constraint, String message) throws BalsaException
    {
        Balsa().require(constraint, message);
    }

    protected <E extends Exception> void require(boolean constraint, E securityException) throws E
    {
        Balsa().require(constraint, securityException);
    }

    /**
     * Check that the current user is valid (not public)
     * 
     * returns boolean
     */
    protected boolean user()
    {
        return Balsa().user();
    }

    /**
     * Check that the current user has the given permission
     * 
     * @param permission
     *            the permission name
     * @return returns boolean
     */
    protected boolean permission(String permission)
    {
        return Balsa().permission(permission);
    }
    
    /**
     * Get the named session variable
     * @param name the variable name
     * @return
     * returns Object
     */
    public Object sessionVar(String name)
    {
        return Balsa().sessionVar(name);
    }
    
    /**
     * Get the named session variable of the given type
     * @param name the variable name
     * @param type the variable type
     * @return
     * returns T
     */
    public <T> T sessionVar(String name, Class<T> type)
    {
        return Balsa().sessionVar(name, type);
    }
    
    /**
     * Store a variable in the session
     * @param name the variable name
     * @param object the variable
     * returns void
     */
    public void sessionVar(String name, Object object)
    {
        Balsa().sessionVar(name, object);
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
    protected <T> T sessionModel(String name, Class<T> type)
    {
        return Balsa().sessionModel(name, type);
    }
    
    protected <T> T sessionModel(String name, Class<T> type, boolean create)
    {
        return Balsa().sessionModel(name, type, create);
    }
    
    protected <T> T sessionModel(String name, T model)
    {
        return Balsa().sessionModel(name, model);
    }
    
    protected Object action(String name, Object... arguments) throws BalsaException
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
}
