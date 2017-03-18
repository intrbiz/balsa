package com.intrbiz.balsa;

import static com.intrbiz.Util.*;

import java.io.IOException;
import java.io.StringWriter;
import java.security.Principal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.bean.BeanProvider;
import com.intrbiz.balsa.engine.SecurityEngine.ValidationLevel;
import com.intrbiz.balsa.engine.security.AuthenticationResponse;
import com.intrbiz.balsa.engine.security.AuthenticationState;
import com.intrbiz.balsa.engine.security.challenge.AuthenticationChallenge;
import com.intrbiz.balsa.engine.security.credentials.Credentials;
import com.intrbiz.balsa.engine.security.credentials.PasswordCredentials;
import com.intrbiz.balsa.engine.security.info.AuthenticationInfo;
import com.intrbiz.balsa.engine.session.BalsaSession;
import com.intrbiz.balsa.engine.task.BalsaTaskState;
import com.intrbiz.balsa.engine.task.DeferredActionTask;
import com.intrbiz.balsa.engine.view.BalsaView;
import com.intrbiz.balsa.error.BalsaIOError;
import com.intrbiz.balsa.error.BalsaInternalError;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.balsa.listener.BalsaRequest;
import com.intrbiz.balsa.listener.BalsaResponse;
import com.intrbiz.balsa.parameter.Parameter;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.balsa.util.CookieBuilder;
import com.intrbiz.balsa.util.HTMLWriter;
import com.intrbiz.converter.ConversionException;
import com.intrbiz.express.DefaultContext;
import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.ExpressEntityResolver;
import com.intrbiz.express.action.ActionHandler;
import com.intrbiz.validator.ValidationException;

/**
 * The balsa Context - represents the state of the Balsa application at any given moment in time.
 * 
 * The Context is bound to the current thread and therefore transient. The Context is only valid for the life time of the request.
 */
public class BalsaContext
{
    protected final static ThreadLocal<BalsaContext> currentInstance = new ThreadLocal<BalsaContext>();

    private final BalsaApplication application;

    private BalsaSession session;

    private final BalsaRequest request;

    private final BalsaResponse response;

    private final Map<String, Object> models = new TreeMap<String, Object>();
    
    private final Map<String, Object> vars = new TreeMap<String, Object>();

    private Throwable exception = null;

    private long processingStart;

    private long processingEnd;

    private final ExpressContext expressContext;

    private BalsaView view;
    
    private final List<ConversionException> conversionErrors = new LinkedList<ConversionException>();
    
    private final List<ValidationException> validationErrors = new LinkedList<ValidationException>();
    
    private Principal currentPrincipal;

    public BalsaContext(BalsaApplication application, BalsaRequest request, BalsaResponse response)
    {
        super();
        this.application = application;
        this.request = request;
        this.response = response;
        this.expressContext = new DefaultContext(this.application.expressExtensions(), new ExpressEntityResolver()
        {
            @Override
            public Object getEntity(String name, Object source)
            {
                if ("balsa".equals(name)) return BalsaContext.this;
                if ("currentPrincipal".equals(name)) return BalsaContext.this.currentPrincipal();
                Object value = BalsaContext.this.getEntity(name);
                if (value != null) return value;
                // next session
                if (BalsaContext.this.session != null)
                {
                    value = BalsaContext.this.session.getEntity(name);
                }
                return value;
            }

            @Override
            public ActionHandler getAction(String name, Object source)
            {
                return BalsaContext.this.application.action(name);
            }
        });
    }
    
    /**
     * Create a request-less BalsaContext
     */
    public BalsaContext(BalsaApplication application)
    {
        this(application, null, null);
    }
    
    /**
     * Create a request-less BalsaContext with the given session
     */
    public BalsaContext(BalsaApplication application, BalsaSession session)
    {
        this(application, null, null);
        this.session = session;
    }

    /**
     * Get the current instance of the balsa context.
     */
    public final static BalsaContext get()
    {
        return currentInstance.get();
    }

    /**
     * Set the current instance of the balsa context.
     */
    public final static void set(BalsaContext context)
    {
        if (context == null)
            currentInstance.remove();
        else
            currentInstance.set(context);
    }
    
    /**
     * Bind this BalsaContext instance to the current thread
     * @return
     */
    public BalsaContext bind()
    {
        BalsaContext.set(this);
        return this;
    }
    
    /**
     * Clear current instance of the balsa context.
     */
    public final static void clear()
    {
        currentInstance.remove();
    }
    
    /**
     * Unbind the BalsaContext for the current thread
     */
    public void unbind()
    {
        BalsaContext.clear();
    }

    public final ExpressContext getExpressContext()
    {
        return this.expressContext;
    }

    /**
     * Get the current application
     * 
     * @return returns balsaApplication
     */
    @SuppressWarnings("unchecked")
    public final <T extends BalsaApplication> T app()
    {
        return (T) this.application;
    }

    /**
     * Get the current session
     * 
     * @return returns BalsaSession
     */
    public final BalsaSession session()
    {
        if (this.session == null)
        {
            String sessionId = this.app().getSessionEngine().makeId();
            this.setSession(this.app().getSessionEngine().getSession(sessionId));
            // send the cookie
            if (this.response().isHeadersSent()) throw new BalsaInternalError("Cannot create session, headers have already been sent.");
            this.response().cookie().name(BalsaSession.COOKIE_NAME).value(sessionId).path(this.path("/")).httpOnly().secure(request().isSecure()).set();
        }
        return session;
    }

    /**
     * Set the current session
     * 
     * @param session
     *            returns void
     */
    public final void setSession(BalsaSession session)
    {
        this.session = session;
    }

    /**
     * Get the balsa request object
     */
    public final BalsaRequest request()
    {
        return this.request;
    }

    /**
     * Get the balsa response object
     */
    public final BalsaResponse response()
    {
        return this.response;
    }

    /**
     * Get the exception with cause the application error
     * 
     * @return returns Throwable
     */
    public final Throwable getException()
    {
        return exception;
    }

    /**
     * Set the exception which caused the application error
     * 
     * @param exception
     *            The exception which ocurred
     */
    public final void setException(Throwable exception)
    {
        this.exception = exception;
    }

    /**
     * Reset this current context
     */
    public void deactivate()
    {
        try
        {
            // return all beans to the providers
            for (Object bean : this.models.values())
            {
                this.application.deactivateModel(bean);
            }
            this.models.clear();
            this.vars.clear();
            this.conversionErrors.clear();
            this.validationErrors.clear();
            this.exception = null;
            this.view = null;
            this.session = null;
            this.currentPrincipal = null;
        }
        catch (Exception e)
        {
            Logger.getLogger(BalsaContext.class).fatal("Error whilst deactivating context", e);
        }
    }

    public void activate()
    {
        // initialise the default principal
        this.currentPrincipal = this.app().getSecurityEngine().defaultPrincipal();
    }
    
    // conversion errors
    
    public boolean hasConversionErrors()
    {
        return ! this.conversionErrors.isEmpty();
    }
    
    public void addConversionError(ConversionException cex)
    {
        this.conversionErrors.add(cex);
    }
    
    public List<ConversionException> getConversionErrors()
    {
        return this.conversionErrors;
    }
    
    public void clearConversionErrors()
    {
        this.conversionErrors.clear();
    }
    
    // validation
    
    public boolean hasValidationErrors()
    {
        return ! this.validationErrors.isEmpty();
    }
    
    public void addValidationError(ValidationException cex)
    {
        this.validationErrors.add(cex);
    }
    
    public List<ValidationException> getValidationErrors()
    {
        return this.validationErrors;
    }
    
    public void clearValidationErrors()
    {
        this.validationErrors.clear();
    }

    // Timing

    public final long getProcessingStart()
    {
        return processingStart;
    }

    public final void setProcessingStart(long processingStart)
    {
        this.processingStart = processingStart;
    }

    public final long getProcessingEnd()
    {
        return processingEnd;
    }

    public final void setProcessingEnd(long processingEnd)
    {
        this.processingEnd = processingEnd;
    }

    // Helper functions

    /**
     * Create the request bean of the given name
     * 
     * @param name
     *            the model name
     * @param type
     *            the model class
     * @return returns Object the bean
     */
    @SuppressWarnings("unchecked")
    public <T> T model(String name, Class<T> type, boolean create)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        // find the bean
        T model = (T) this.models.get(name);
        if (model == null && create)
        {
            // create the bean
            model = this.application.activateModel(type);
            if (model != null)
            {
                this.models.put(name, model);
            }
        }
        return model;
    }
    
    public <T> T model(String name, Class<T> type)
    {
        return this.model(name, type, true);
    }

    public Object model(String name)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        return this.models.get(name);
    }

    public <T> T model(String name, T model)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        this.models.put(name, model);
        return model;
    }

    public <E> BeanProvider<E> provider(Class<E> type)
    {
        return this.application.provider(type);
    }
    
    /**
     * Get the named variable
     * @param name the variable name
     * @return
     * returns Object
     */
    @SuppressWarnings("unchecked")
    public <T> T var(String name)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        return (T) this.vars.get(name);
    }
    
    /**
     * Store a variable
     * @param name the variable name
     * @param object the variable
     * returns void
     */
    public <T> T var(String name, T object)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        this.vars.put(name, object);
        return object;
    }
    
    /**
     * Remove a variable of the given name
     * @param name
     */
    public void removeVar(String name)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        this.vars.remove(name);
    }
    
    /**
     * Get the model or variable with the given name
     * @param name the model or variable name
     * @return
     */
    public Object getEntity(String name)
    {
        // try request var first
        Object value = this.var(name);
        if (value != null) return value;
        // next request model
        value = this.model(name);
        if (value != null) return value;
        return value;
    }

    /**
     * Get the value of the request parameter given
     * 
     * @param name
     *            the parameter name
     * @return returns String the parameter value
     */
    public String param(String name)
    {
        Parameter p = this.request.getParameter(name);
        if (p != null) return p.getStringValue();
        return null;
    }
    
    /**
     * Get the value of the request parameter given
     * @param name the parameter name
     * @return the List&lt;String&gt; parameter value or null
     */
    public List<String> listParam(String name)
    {
        Parameter p = this.request.getParameter(name);
        if (p != null) return p.getStringListValue();
        return new LinkedList<String>();
    }

    /**
     * Get the value of the request header given
     * 
     * @param name
     *            the header name
     * @return returns String the header value
     */
    public String header(String name)
    {
        return this.request().getHeader(name);
    }

    /**
     * Get the value of the request cookie given
     * 
     * @param name
     *            the cookie name
     * @return returns String the cookie value
     */
    public String cookie(String name)
    {
        return this.request().cookie(name);
    }
    
    /**
     * Set a cookie.  Cookies are set using a fluent interface, 
     * for example: <code>cookie().name("name").value("value").set()</code>
     * @return A CookieBuilder to create and set the cookie.
     */
    public CookieBuilder<BalsaResponse> cookie()
    {
        return this.response.cookie();
    }

    /**
     * Get the view which is currently being decoded or encoded
     * 
     * @return the BalsaView
     */
    public BalsaView view()
    {
        return this.view;
    }

    /**
     * Get the view which is currently being decoded or encoded
     * 
     * @return the BalsaView
     */
    public BalsaView getView()
    {
        return this.view;
    }

    /**
     * Decode the given views
     * 
     * @param useTemplate
     *            should the view engine use the configured application templates
     * @param views
     *            returns void
     */
    public void decode(String[][] templates, String... views) throws BalsaException
    {
        try
        {
            this.view = this.app().getViewEngine().load(templates, views, this);
            this.view.decode(this);
        }
        finally
        {
            this.view = null;
        }
    }

    public void decodeOnly(String... views) throws BalsaException
    {
        this.decode(null, views);
    }

    /**
     * Respond by encoding the given views
     * 
     * NB: This will not set the content type and status of the response.
     * 
     * @param views
     *            the views to encode
     * @throws BalsaException
     *             returns void
     */
    public void encode(BalsaWriter to, String[][] templates, String... views) throws BalsaException
    {
        try
        {
            try
            {
                this.view = this.app().getViewEngine().load(templates, views, this);
                // encode
                if (to == null) to = this.response().getViewWriter();
                this.view.encode(this, to);
                // flush the response
                to.flush();
            }
            finally
            {
                this.view = null;
            }
        }
        catch (IOException e)
        {
            throw new BalsaIOError("IO error while encoding view", e);
        }
    }

    public void encode(String[][] templates, String... views) throws BalsaException
    {
        this.encode(null, templates, views);
    }

    public void encodeOnly(BalsaWriter to, String... views) throws BalsaException
    {
        this.encode(to, null, views);
    }

    public void encodeOnly(String... views) throws BalsaException
    {
        this.encode(null, null, views);
    }

    public String encodeBuffered(String[][] templates, String... views) throws BalsaException
    {
        try
        {
            StringWriter sw = new StringWriter();
            HTMLWriter hw = new HTMLWriter(sw);
            this.encode(hw, templates, views);
            hw.close();
            return sw.toString();
        }
        catch (IOException e)
        {
            throw new BalsaIOError("IO error while encoding buffered view", e);
        }
    }

    public String encodeOnlyBuffered(String... views) throws BalsaException
    {
        return this.encodeBuffered(null, views);
    }
    
    public void encodeInclude(BalsaWriter to, String... views) throws BalsaException
    {
        try
        {
            BalsaView view = this.app().getViewEngine().load(null, views, this);
            // encode
            if (to == null) to = this.response().getViewWriter();
            view.encode(this, to);
        }
        catch (IOException e)
        {
            throw new BalsaIOError("IO error while encoding view", e);
        }
    }
    
    public String encodeIncludeBuffered(String... views) throws BalsaException
    {
        try
        {
            StringWriter sw = new StringWriter();
            HTMLWriter hw = new HTMLWriter(sw);
            this.encodeInclude(hw, views);
            hw.close();
            return sw.toString();
        }
        catch (IOException e)
        {
            throw new BalsaIOError("IO error while encoding buffered view", e);
        }
    }

    /**
     * Translate the given URL into an absolute URL using information from the request.
     * 
     * The URL will not be translated, and returned unchanged, if: 1. the URL has a protocol (start with: '[A-Za-z]://') 2. the URL starts with '//'
     * 
     * @param url
     *            the relative url to translate
     * @return returns String
     */
    public String url(String url)
    {
        if (url == null) return null;
        if (url.startsWith("#")) return url;
        if (url.startsWith("//")) return url;
        if (url.indexOf("://") != -1) return url;
        // translate it
        StringBuilder sb = new StringBuilder();
        // the scheme
        int port = this.request.getServerPort();
        String scheme = this.request.getRequestScheme();
        sb.append(scheme != null ? scheme : (port == 443 ? "https" : "http"));
        sb.append("://");
        // server name
        sb.append(this.request.getServerName());
        // port?
        if (port != 80 && port != 443) sb.append(":").append(port);
        // script path
        String scriptPath = this.request.getScriptName();
        if (scriptPath.length() > 0)
        {
            if (!scriptPath.startsWith("/")) sb.append("/");
            sb.append(scriptPath);
        }
        // path
        if (!(url.startsWith("/") || scriptPath.endsWith("/"))) sb.append("/");
        sb.append(url);
        return sb.toString();
    }

    /**
     * Translate the given path to a server absolute path using information from the request.
     * 
     * @param path
     *            the path to make absolute
     * @return returns String
     */
    public String path(String path)
    {
        StringBuilder sb = new StringBuilder();
        // script path
        String scriptPath = this.request.getScriptName();
        if (scriptPath.length() > 0)
        {
            if (!scriptPath.startsWith("/")) sb.append("/");
            sb.append(scriptPath);
        }
        // path
        if (!(path.startsWith("/") || scriptPath.endsWith("/"))) sb.append("/");
        sb.append(path);
        return sb.toString();
    }

    /**
     * Translate the given relative path into the URL for the public resource.
     * 
     * The resulting URL maybe an absolute server path or an absolute URL.
     * 
     * 
     * 
     * @param path
     *            the relative path to the public resource
     * @return the URL to the resource.
     */
    public String pub(String path)
    {
        return this.app().getPublicResourceEngine().pub(this, path);
    }

    /**
     * Redirect to another URL
     * 
     * @param url
     *            the URL to redirect to
     * @param permanent
     *            it the redirect permenant (301) returns void
     */
    public void redirect(String url, boolean permanent) throws IOException
    {
        this.response.redirect(this.url(url), permanent);
    }

    /**
     * Redirect (302) to another URL
     * 
     * @param url
     *            the URL to redirect to returns void
     */
    public void redirect(String url) throws IOException
    {
        redirect(url, false);
    }

    /**
     * Require a security constraint to be met
     * 
     * @param constraint
     *            returns void
     */
    public void require(boolean constraint) throws BalsaException
    {
        if (!constraint) throw new BalsaSecurityException("Secuirty requirement not met");
    }

    public void require(boolean constraint, String message) throws BalsaException
    {
        if (!constraint) throw new BalsaSecurityException(message);
    }

    public <E extends Exception> void require(boolean constraint, E securityException) throws E
    {
        if (!constraint) throw securityException;
    }
    
    /**
     * No authentication is currently happening or has not happened
     */
    public boolean notAuthenticated()
    {
        return this.session().authenticationState().isNotAuthenticated();
    }
    
    /**
     * Authentication is currently in progress
     */
    public boolean authenticating()
    {
        return this.session().authenticationState().isAuthenticating();
    }
    
    /**
     * Do we have an authenticated principal
     */
    public boolean authenticated()
    {
        return this.session().authenticationState().isAuthenticated();
    }
    
    /**
     * Get the authentication state for this current session
     */
    public AuthenticationState authenticationState()
    {
        return this.session().authenticationState();
    }
    
    /**
     * Get the authentication info for this current session
     */
    public AuthenticationInfo authenticationInfo()
    {
        return this.session().authenticationState().info();
    }

    /**
     * Check that the current principal is valid
     */
    public boolean validPrincipal(ValidationLevel validationLevel)
    {
        // delegate validation to the security manager
        return this.app().getSecurityEngine().isValidPrincipal(this.currentPrincipal(), validationLevel);
    }
    
    /**
     * Check that the current principal is valid (strongly)
     */
    public boolean validPrincipal()
    {
        return this.validPrincipal(ValidationLevel.STRONG);
    }
    
    /**
     * Check that the current principal is valid (weakly)
     */
    public boolean principal()
    {
        return this.validPrincipal(ValidationLevel.WEAK);
    }

    /**
     * Get the current logged in principal from the request or the session
     */
    @SuppressWarnings("unchecked")
    public <T extends Principal> T currentPrincipal()
    {
        return this.currentPrincipal != null ? (T) this.currentPrincipal : this.session().authenticationState().currentPrincipal();
    }

    /**
     * Deauthenticate the current logged in principal
     */
    public void deauthenticate()
    {
        this.currentPrincipal = null;
        if (this.session != null) this.session.authenticationState().reset();
    }
    
    /**
     * Verify the given credentials are valid for the currently authenticated principal
     * @throws BalsaSecurityException should there be any issues verifying the credentials for the currently authenticated principal
     */
    public void verify(Credentials credentials) throws BalsaSecurityException
    {
        this.app().getSecurityEngine().verify(this.session().authenticationState(), credentials);
    }
    
    /**
     * Generate a set of authentication challenges for the currently authenticating principal. 
     * This is a map of the authentication method name to challenge.
     */
    public Map<String, AuthenticationChallenge> generateAuthenticationChallenges()
    {
        Principal principal = this.session().authenticationState().authenticatingPrincipal();
        if (principal == null) throw new BalsaException("There is no principal which is currently authenticating, cannot generate authentication challenges");
        return this.app().getSecurityEngine().generateAuthenticationChallenges(principal);
    }
    
    /**
     * Generate a set of challenges which are needed to authenticate the given 
     * principal via certain authentication methods.  This is a map of the 
     * authentication method name to challenge.
     */
    public Map<String, AuthenticationChallenge> generateAuthenticationChallenges(Principal principal)
    {
        return this.app().getSecurityEngine().generateAuthenticationChallenges(principal);
    }
    
    /**
     * Start the authentication process.  The response will specify if
     * @throws BalsaSecurityException should there be any issues authenticating the user.
     */
    public AuthenticationResponse authenticate(Credentials credentials) throws BalsaSecurityException
    {
        // use the security engine to authenticate the user
        AuthenticationResponse response = this.app().getSecurityEngine().authenticate(this.session().authenticationState(), credentials);
        // update the authentication state
        return this.session().authenticationState().update(response);
    }

    /**
     * Authenticate for the life of this session, using a single factor authentication. 
     * This method will always return with a valid, authenticated user.
     * @throws BalsaSecurityException should there be any issues authenticating the user.
     */
    @SuppressWarnings("unchecked")
    public <T extends Principal> T authenticateSingleFactor(Credentials credentials) throws BalsaSecurityException
    {
        // use the security engine to authenticate the user
        AuthenticationResponse response = this.app().getSecurityEngine().authenticate(this.session().authenticationState(), credentials);
        if (! response.isComplete()) throw new BalsaSecurityException("Failed to authenticate user using single factor");
        // update the authentication state
        return (T) this.session().authenticationState().update(response).getPrincipal();
    }

    /**
     * Authenticate for the life of this session, using single factor authentication.  
     * This method will always return with a valid, authenticated user.
     * @throws BalsaSecurityException should there be any issues authenticating the user.
     */
    public <T extends Principal> T authenticate(String username, String password) throws BalsaSecurityException
    {
        return this.authenticateSingleFactor(new PasswordCredentials.Simple(username, password));
    }
    
    /**
     * Authenticate for the life of this request only, this avoids creating a session, 
     * this method will always return with a valid, authenticated user.
     * @throws BalsaSecurityException should there be any issues authenticating the user.
     */
    @SuppressWarnings("unchecked")
    public <T extends Principal> T authenticateRequest(Credentials credentials) throws BalsaSecurityException
    {
        // use the security engine to authenticate the user
        Principal principal = this.app().getSecurityEngine().authenticateRequest(credentials);
        if (principal == null) throw new BalsaSecurityException("Failed to authenticate user");
        // store the principal
        this.currentPrincipal = principal;
        return (T) principal;
    }
    
    /**
     * Authenticate for the life of this request only, this avoids creating a session, 
     * this method will always return with a valid, authenticated user.
     * @throws BalsaSecurityException should there be any issues authenticating the user.
     */
    public <T extends Principal> T authenticateRequest(String username, String password) throws BalsaSecurityException
    {
        return this.authenticateRequest(new PasswordCredentials.Simple(username, password));
    }
    
    /**
     * Try to authenticate for the life of this session, should 
     * authentication not be possible then null is returned, exceptions are thrown.
     */
    public <T extends Principal> T tryAuthenticate(String username, String password)
    {
        try 
        {
            return this.authenticate(username, password);
        }
        catch (BalsaSecurityException e)
        {
            // ignore
        }
        return null;
    }

    /**
     * Try to authenticate for the life of this session, should 
     * authentication not be possible then null is returned, exceptions are thrown.
     */
    public <T extends Principal> T tryAuthenticateSingleFactor(Credentials credentials)
    {
        try 
        {
            return this.authenticateSingleFactor(credentials);
        }
        catch (BalsaSecurityException e)
        {
            // ignore
        }
        return null;
    }
    
    /**
     * Try to authenticate for the life of this session, should 
     * authentication not be possible then null is returned, exceptions are thrown.
     */
    public AuthenticationResponse tryAuthenticate(Credentials credentials)
    {
        try 
        {
            return this.authenticate(credentials);
        }
        catch (BalsaSecurityException e)
        {
            // ignore
        }
        return null;
    }
    
    /**
     * Try to authenticate for the life of this request only, this avoids creating a session, 
     * should authentication not be possible then null is returned, exceptions are thrown.
     */
    public <T extends Principal> T tryAuthenticateRequest(String username, String password)
    {
        try 
        {
            return this.authenticateRequest(username, password);
        }
        catch (BalsaSecurityException e)
        {
            // ignore
        }
        return null;
    }
    
    /**
     * Try to authenticate for the life of this request only, this avoids creating a session, 
     * should authentication not be possible then null is returned, exceptions are thrown.
     */
    public <T extends Principal> T tryAuthenticateRequest(Credentials credentials)
    {
        try 
        {
            return this.authenticateRequest(credentials);
        }
        catch (BalsaSecurityException e)
        {
            // ignore
        }
        return null;
    }

    /**
     * Check that the current user has the given permission
     * 
     * @param permission
     *            the permission name
     * @return returns boolean
     */
    public boolean permission(String permission)
    {
        return this.app().getSecurityEngine().check(this.currentPrincipal(), permission);
    }

    /**
     * Check that the current user has the given permission over the given object
     * @param permission the permission name
     * @param object the object over which permission must be granted
     * @return true if and only if the current user has the given permission over th given object
     */
    public boolean permission(String permission, Object object)
    {
        return this.app().getSecurityEngine().check(this.currentPrincipal(), permission, object);
    }

    /**
     * Check that the given access token is valid
     * 
     * @param token
     * @return
     */
    public boolean validAccessToken(String token)
    {
        if (isEmpty(token)) return false;
        return this.application.getSecurityEngine().validAccess(token);
    }

    /**
     * Check that the given access token is valid for the current path
     * 
     * @param token
     * @return
     */
    public boolean validAccessTokenForURL(String token)
    {
        if (isEmpty(token)) return false;
        return this.application.getSecurityEngine().validAccessForURL(this.request.getPathInfo(), token);
    }

    /**
     * Generate an access token which is valid for the given URL
     * 
     * @param path
     * @return
     */
    public String generateAccessTokenForURL(String path)
    {
        return this.application.getSecurityEngine().generateAccessTokenForURL(path);
    }
    
    /**
     * Generate an access token
     * @return
     */
    public String generateAccessToken()
    {
        return this.application.getSecurityEngine().generateAccessToken();
    }

    /**
     * Get the named session variable
     * 
     * @param name
     *            the variable name
     * @return returns Object
     */
    @SuppressWarnings("unchecked")
    public <T> T sessionVar(String name)
    {
        return (T) this.session().var(name);
    }

    /**
     * Store a variable in the session
     * 
     * @param name
     *            the variable name
     * @param object
     *            the variable returns void
     */
    public <T> T sessionVar(String name, T object)
    {
        return this.session().var(name, object);
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
    public <T> T sessionModel(String name, Class<T> type, boolean create)
    {
        return this.session().model(name, type, create);
    }
    
    public <T> T sessionModel(String name, Class<T> type)
    {
        return this.sessionModel(name, type, true);
    }

    public <T> T sessionModel(String name, T model)
    {
        return this.session().model(name, model);
    }

    // Actions

    /**
     * Execute the named action with the given arguments
     * @param action the action name
     * @param arguments the arguments to pass to the action
     * @return the result of the action
     * @throws Exception should the action fail for any reason
     */
    @SuppressWarnings("unchecked")
    public <T> T action(String action, Object... arguments) throws Exception
    {
        ActionHandler handler = this.app().action(action);
        if (handler == null) throw new BalsaException("The action " + action + " does not exist");
        return (T) handler.act(arguments);
    }
    
    /**
     * Execute the named action with the given arguments using the task engine, 
     * this causes the task to be executed out of band.  This returns a task id 
     * which can be used to poll the state of a task.
     * @param action the action name
     * @param arguments the arguments to pass to the action
     * @return the id of the started task
     */
    public String deferredAction(String action, Object... arguments)
    {
        // generate a random id
        final String id = UUID.randomUUID().toString();
        // store the initial task state
        this.session().task(id, new BalsaTaskState());
        // submit the task for execution
        this.app().getTaskEngine().executeTask(new DeferredActionTask(action, arguments), id);
        // return the id
        return id;
    }
    
    /**
     * Execute the deferred action with a know id
     */
    public String deferredActionWithId(final String id, String action, Object... arguments)
    {
        // store the initial task state
        this.session().task(id, new BalsaTaskState());
        // submit the task for execution
        this.app().getTaskEngine().executeTask(new DeferredActionTask(action, arguments), id);
        // return the id
        return id;
    }
    
    /**
     * Poll the state of a deferred action, getting and removing it 
     * should it have completed
     */
    public BalsaTaskState pollDeferredAction(String id)
    {
        return this.session().removeTaskIfComplete(id);
    }

    // Static

    public final static BalsaContext Balsa()
    {
        return BalsaContext.get();
    }
    
    public final static <T> T withContext(BalsaContext context, Callable<T> task) throws Exception
    {
        try
        {
            // activate the context
            context.activate();
            // bind the context
            context.bind();
            // execute the task
            return task.call();
        }
        finally
        {
            // ensure the context is unbound
            context.unbind();
            // deactivate the context
            context.deactivate();
        }
    }
    
    public final static <T> T withContext(BalsaApplication application, BalsaSession session, Callable<T> task) throws Exception
    {
        return withContext(new BalsaContext(application, session), task);
    }
}
