package com.intrbiz.balsa;

import static com.intrbiz.Util.*;

import java.io.IOException;
import java.io.StringWriter;
import java.security.Principal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.intrbiz.balsa.bean.BeanProvider;
import com.intrbiz.balsa.engine.security.Credentials;
import com.intrbiz.balsa.engine.security.PasswordCredentials;
import com.intrbiz.balsa.engine.session.BalsaSession;
import com.intrbiz.balsa.engine.view.BalsaView;
import com.intrbiz.balsa.error.BalsaIOError;
import com.intrbiz.balsa.error.BalsaInternalError;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.balsa.listener.BalsaRequest;
import com.intrbiz.balsa.listener.BalsaResponse;
import com.intrbiz.balsa.parameter.Parameter;
import com.intrbiz.balsa.parameter.StringParameter;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.balsa.util.HTMLWriter;
import com.intrbiz.converter.ConversionException;
import com.intrbiz.express.DefaultContext;
import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.ExpressEntityResolver;
import com.intrbiz.express.action.ActionHandler;
import com.intrbiz.validator.ValidationException;

;

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

    private Throwable exception = null;

    private long processingStart;

    private long processingEnd;

    private final ExpressContext expressContext;

    private BalsaView view;
    
    private final List<ConversionException> conversionErrors = new LinkedList<ConversionException>();
    
    private final List<ValidationException> validationErrors = new LinkedList<ValidationException>();

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
                if ("balsa".equals(name)) return this;
                Object value = BalsaContext.this.models.get(name);
                if (value != null) return value;
                // next session
                if (BalsaContext.this.session != null)
                {
                    value = BalsaContext.this.session.getEntity(name, source);
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

    public final ExpressContext getExpressContext()
    {
        return this.expressContext;
    }

    /**
     * Get the current application
     * 
     * @return returns balsaApplication
     */
    public final BalsaApplication app()
    {
        return this.application;
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
            this.response().header("Set-Cookie", BalsaSession.COOKIE_NAME + "=" + sessionId + "; Path=" + this.path("/") + "; HttpOnly");
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
        // return all beans to the providers
        for (Object bean : this.models.values())
        {
            this.application.deactivateModel(bean);
        }
        this.models.clear();
        this.conversionErrors.clear();
        this.validationErrors.clear();
        this.exception = null;
    }

    public void activate()
    {
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
        return this.models.get(name);
    }

    public <T> T model(String name, T model)
    {
        this.models.put(name, model);
        return model;
    }

    public <E> BeanProvider<E> provider(Class<E> type)
    {
        return this.application.provider(type);
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
        if (p instanceof StringParameter) return p.getStringValue();
        return null;
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
            if (to == null) to = this.response().getViewWriter();
            // load and encode the view
            try
            {
                this.view = this.app().getViewEngine().load(templates, views, this);
                this.view.encode(this, to);
            }
            finally
            {
                this.view = null;
            }
            // flush the response
            to.flush();
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
     * Check that the current principal is valid (not public)
     * 
     * returns boolean
     */
    public boolean validPrincipal()
    {
        return this.currentPrincipal() != null;
    }

    public Principal currentPrincipal()
    {
        return this.session().currentPrincipal();
    }

    public void deauthenticate()
    {
        this.session().setCurrentPrincipal(null);
    }

    public Principal authenticate(Credentials credentials) throws BalsaSecurityException
    {
        // use the security engine to authenticate the user
        Principal principal = this.app().getSecurityEngine().authenticate(credentials);
        if (principal == null) throw new BalsaSecurityException("Failed to authenticate user");
        // store the principal
        this.session().setCurrentPrincipal(principal);
        //
        return principal;
    }

    public Principal authenticate(String username, String password) throws BalsaSecurityException
    {
        return this.authenticate(new PasswordCredentials.Simple(username, password));
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
    public Object sessionVar(String name)
    {
        return this.session().var(name);
    }

    /**
     * Get the named session variable of the given type
     * 
     * @param name
     *            the variable name
     * @param type
     *            the variable type
     * @return returns T
     */
    public <T> T sessionVar(String name, Class<T> type)
    {
        return this.session().var(name, type);
    }

    /**
     * Store a variable in the session
     * 
     * @param name
     *            the variable name
     * @param object
     *            the variable returns void
     */
    public void sessionVar(String name, Object object)
    {
        this.session().var(name, object);
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
    public <T> T sessionModel(String name, Class<T> type)
    {
        return this.session().model(name, type);
    }

    public <T> T sessionModel(String name, Class<T> type, boolean create)
    {
        return this.session().model(name, type, create);
    }

    public <T> T sessionModel(String name, T model)
    {
        return this.session().model(name, model);
    }

    // Actions

    public Object action(String action, Object... arguments) throws Exception
    {
        ActionHandler handler = this.app().action(action);
        if (handler == null) throw new BalsaException("The action " + action + " does not exist");
        return handler.act(arguments);
    }

    // Static

    public final static BalsaContext Balsa()
    {
        return BalsaContext.get();
    }
}
