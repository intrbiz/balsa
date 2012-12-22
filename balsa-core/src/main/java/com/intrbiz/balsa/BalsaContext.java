package com.intrbiz.balsa;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.intrbiz.balsa.bean.BeanProvider;
import com.intrbiz.balsa.engine.session.BalsaSession;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.balsa.listener.BalsaRequest;
import com.intrbiz.balsa.listener.BalsaResponse;
import com.intrbiz.balsa.parameter.Parameter;
import com.intrbiz.balsa.parameter.StringParameter;
import com.intrbiz.balsa.scgi.SCGIResponse.Status;
import com.intrbiz.balsa.util.BalsaELContext;
import com.intrbiz.express.ELContext;
import com.intrbiz.express.action.ActionHandler;

/**
 * The balsa Context - represents the state of the JSC application at any given moment in time.
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
    
    private final ELContext elContext = new BalsaELContext() {
        @Override
        public Object getEntityInner(String name, Object source)
        {
            return BalsaContext.this.models.get(name);
        }

        @Override
        public BalsaApplication getApplication()
        {
            return BalsaContext.this.getApplication();
        }
    };

    public BalsaContext(BalsaApplication application, BalsaRequest request, BalsaResponse response)
    {
        super();
        this.application = application;
        this.request = request;
        this.response = response;
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
    
    public final ELContext getELContext()
    {
        return this.elContext;
    }

    /**
     * Get the current application
     * 
     * @return returns balsaApplication
     */
    public final BalsaApplication getApplication()
    {
        return this.application;
    }

    /**
     * Get the current session
     * @return
     * returns BalsaSession
     */
    public final BalsaSession getSession()
    {
        return session;
    }

    /**
     * Set the current session
     * @param session
     * returns void
     */
    public final void setSession(BalsaSession session)
    {
        this.session = session;
    }

    /**
     * Get the balsa request object
     */
    public final BalsaRequest getRequest()
    {
        return this.request;
    }

    /**
     * Get the balsa response object
     */
    public final BalsaResponse getResponse()
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
        this.exception = null;
    }

    public void activate()
    {
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
     * Decode the given views
     * 
     * @param useTemplate should the view engine use the configured application templates
     * @param views
     *            returns void
     */
    public void decode(boolean useTemplate, String... views) throws BalsaException
    {
        this.getApplication().getViewEngine().load(views, useTemplate, this).decode(this);
    }
    
    public void decode(String... views) throws BalsaException
    {
        this.decode(true, views);
    }


    /**
     * Set the response content type and status and encode the given views.  
     * @param contentType the content type of the response
     * @param status      the status of the response
     * @param useTemplate should the view engine use the configured application templates
     * @param views       the views to encode
     * @throws BalsaException
     * returns void
     */
    public void encode(String contentType, Status status, boolean useTemplate, String... views) throws BalsaException
    {
        try
        {
            // set the content type
            this.response.contentType(contentType);
            // set the response status
            this.response.status(status);
            // load and encode the view
            this.application.getViewEngine().load(views, useTemplate, this).encode(this);
            // flush the response
            this.response.flush();
        }
        catch (IOException e)
        {
            throw new BalsaException("Error encoding view", e);
        }
    }
    
    public void encode(String contentType, Status status, String... views) throws BalsaException
    {
        this.encode(contentType, status, true, views);
    }
    
    /**
     * Respond by encoding the given views
     * 
     * NB: This will not set the content type and status of the response.
     * @param useTemplate should the view engine use the configured application templates
     * @param views  the views to encode
     * @throws BalsaException
     * returns void
     */
    public void encode(boolean useTemplate, String... views) throws BalsaException
    {
        try
        {
            // load and encode the view
            this.application.getViewEngine().load(views, useTemplate, this).encode(this);
            // flush the response
            this.response.flush();
        }
        catch (IOException e)
        {
            throw new BalsaException("Error encoding view", e);
        }
    }
    
    public void encode(String... views) throws BalsaException
    {
        this.encode(true, views);
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
        if (url == null) return null;
        if (url.startsWith("//")) return url;
        // TODO Regex?
        if (url.indexOf("://") != -1) return url;
        // translate it
        StringBuilder sb = new StringBuilder();
        // TODO
        sb.append("http://");
        // server name
        sb.append(this.request.getServerName());
        // port?
        int port = this.request.getServerPort();
        if (port != 80 && port != 443) sb.append(":").append(port);
        // script path
        String scriptPath = this.request.getScriptName();
        if (! scriptPath.startsWith("/")) sb.append("/");
        sb.append(scriptPath);
        // path
        if (! (url.startsWith("/") || scriptPath.endsWith("/"))) sb.append("/");
        sb.append(url);
        return sb.toString();
    }

    /**
     * Translate the given path to a server absolute path using information from the request.
     * @param path the path to make absolute
     * @return
     * returns String
     */
    public String path(String path)
    {
        StringBuilder sb = new StringBuilder();
        // script path
        String scriptPath = this.request.getScriptName();
        if (! scriptPath.startsWith("/")) sb.append("/");
        sb.append(scriptPath);
        // path
        if (! (path.startsWith("/") || scriptPath.endsWith("/"))) sb.append("/");
        sb.append(path);
        return sb.toString();        
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
     * Check that the current user is valid (not public)
     * 
     * returns boolean
     */
    public boolean user()
    {
        return false;
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
        return false;
    }
    
    /**
     * Get the named session variable
     * @param name the variable name
     * @return
     * returns Object
     */
    public Object sessionVar(String name)
    {
        return this.session.var(name);
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
        return this.session.var(name, type);
    }
    
    /**
     * Store a variable in the session
     * @param name the variable name
     * @param object the variable
     * returns void
     */
    public void sessionVar(String name, Object object)
    {
        this.session.var(name, object);
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
        if (this.session == null) return null;
        return this.session.model(name, type);
    }
    
    public <T> T sessionModel(String name, Class<T> type, boolean create)
    {
        if (this.session == null) return null;
        return this.session.model(name, type, create);
    }
    
    public <T> T sessionModel(String name, T model)
    {
        if (this.session == null) return null;
        return this.session.model(name, model);
    }
    
    // Actions
    
    public Object action(String action, Object... arguments) throws Exception
    {
        ActionHandler handler = this.application.action(action);
        if (handler == null) throw new BalsaException("The action " + action + " does not exist");
        return handler.act(arguments);
    }

    // Static

    public final static BalsaContext Balsa()
    {
        return BalsaContext.get();
    }
}
