package com.intrbiz.balsa;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.bean.BeanFactory;
import com.intrbiz.balsa.bean.BeanProvider;
import com.intrbiz.balsa.bean.impl.NonPooledBean;
import com.intrbiz.balsa.bean.impl.PooledBean;
import com.intrbiz.balsa.engine.PublicResourceEngine;
import com.intrbiz.balsa.engine.RouteEngine;
import com.intrbiz.balsa.engine.SecurityEngine;
import com.intrbiz.balsa.engine.SessionEngine;
import com.intrbiz.balsa.engine.ViewEngine;
import com.intrbiz.balsa.engine.impl.publicresource.PublicResourceEngineImpl;
import com.intrbiz.balsa.engine.impl.route.RouteEngineImpl;
import com.intrbiz.balsa.engine.impl.security.SecurityEngineImpl;
import com.intrbiz.balsa.engine.impl.session.SimpleSessionEngine;
import com.intrbiz.balsa.engine.impl.view.BalsaViewEngineImpl;
import com.intrbiz.balsa.engine.impl.view.FileViewSource;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.express.BalsaFunction;
import com.intrbiz.balsa.express.PathFunction;
import com.intrbiz.balsa.express.PathInfoFunction;
import com.intrbiz.balsa.express.PublicFunction;
import com.intrbiz.balsa.express.RequestPathTokenFunction;
import com.intrbiz.balsa.express.RequestTokenFunction;
import com.intrbiz.balsa.express.TitleFunction;
import com.intrbiz.balsa.listener.BalsaFilter;
import com.intrbiz.balsa.listener.BalsaListener;
import com.intrbiz.balsa.listener.BalsaProcessor;
import com.intrbiz.balsa.listener.filter.SessionFilter;
import com.intrbiz.balsa.listener.processor.FilterProcessor;
import com.intrbiz.balsa.listener.processor.RouteProcessor;
import com.intrbiz.balsa.listener.scgi.BalsaSCGIListener;
import com.intrbiz.express.ExpressExtensionRegistry;
import com.intrbiz.express.action.ActionHandler;
import com.intrbiz.express.action.MethodActionHandler;
import com.intrbiz.express.operator.Decorator;
import com.intrbiz.express.operator.Function;
import com.intrbiz.metadata.Pooled;

/**
 * A balsa Application
 */
public abstract class BalsaApplication
{
    private Logger logger = Logger.getLogger(BalsaApplication.class);

    /**
     * Application arguments
     */
    private final Map<String, String> arguments = new HashMap<String, String>();

    /**
     * Bean providers
     */
    private final Map<String, BeanProvider<?>> models = new HashMap<String, BeanProvider<?>>();

    /**
     * Actions
     */
    private final Map<String, ActionHandler> actions = new HashMap<String, ActionHandler>();

    /**
     * The listener
     */
    private BalsaListener listener;

    /**
     * The routing engine
     */
    private final RouteEngine routingEngine = new RouteEngineImpl();

    /**
     * The session engine
     */
    private SessionEngine sessionEngine;

    /**
     * The view engine
     */
    private ViewEngine viewEngine;
    
    /**
     * The security engine
     */
    private SecurityEngine securityEngine;
    
    /**
     * The public resource Engine
     */
    private PublicResourceEngine publicResourceEngine;
    
    /**
     * Where to load views from
     */
    private final List<File> viewPath = new CopyOnWriteArrayList<File>();

    /**
     * Filters for this application
     */
    private final List<BalsaFilter> filters = new LinkedList<BalsaFilter>();
    

    /**
     * The ExpressExtensionRegistry where Balsa stores its extensions
     */
    private final ExpressExtensionRegistry expressExtensions = new ExpressExtensionRegistry("balsa").addSubRegistry(ExpressExtensionRegistry.getDefaultRegistry());
    
    private final String[] templates = this.myTemplates();
    
    private final String[] myTemplates()
    {
        return new String[0];
    }

    public BalsaApplication()
    {
        super();
        /* Default Functions */
        // immutable
        this.expressExtensions.addFunction(new BalsaFunction());
        this.expressExtensions.addFunction(new PathInfoFunction());
        this.expressExtensions.addFunction(new TitleFunction());
        this.expressExtensions.addFunction(new RequestTokenFunction());
        //
        this.expressExtensions.addFunction("path", PathFunction.class);
        this.expressExtensions.addFunction("public", PublicFunction.class);
        this.expressExtensions.addFunction("access_token_for_url", RequestPathTokenFunction.class);
        /* Default Engines */
        this.listener(new BalsaSCGIListener());
        this.sessionEngine(new SimpleSessionEngine());
        this.viewEngine(new BalsaViewEngineImpl());
        this.securityEngine(new SecurityEngineImpl());
        this.publicResourceEngine(new PublicResourceEngineImpl());
    }

    /**
     * Get all application arguments
     * 
     * @return returns Map<String,String>
     */
    public Map<String, String> getArguments()
    {
        return arguments;
    }

    /**
     * Get an application argument
     * 
     * @param name
     *            The argument name
     * @return returns String
     */
    public String getArgument(String name)
    {
        return this.arguments.get(name);
    }

    /**
     * this.routingEngine.s Get an application argument, returning the default value if the argument is null
     * 
     * @param name
     *            The argument name
     * @param def
     *            The argument default value
     * @return returns String
     */
    public String getArgument(String name, String def)
    {
        String val = this.arguments.get(name);
        if (val == null) return def;
        return val;
    }

    /**
     * Get an application argument and parse it as an int, returning the default value if the argument is null
     * 
     * @param name
     *            The argument name
     * @param def
     *            The argument default value
     * @return returns int
     */
    public int getIntArgument(String name, int def)
    {
        String val = this.arguments.get(name);
        if (val == null) return def;
        return Integer.parseInt(val);
    }

    /**
     * Set an application argument
     * 
     * @param name
     *            The argument name
     * @param value
     *            The argument value
     */
    public void argument(String name, String value)
    {
        this.arguments.put(name, value);
    }

    /**
     * Set all application arguments
     * 
     * @param arguments
     *            The arguments
     */
    public void arguments(Map<String, String> arguments)
    {
        this.arguments.putAll(arguments);
    }

    /**
     * Get the listener for this application
     * 
     * @return returns BalsaListener
     */
    public BalsaListener getListener()
    {
        return listener;
    }

    /**
     * Setup the listener which will be used to process requests from the web server
     * 
     * @param listener
     *            The listener to use
     */
    public void listener(BalsaListener listener)
    {
        this.listener = listener;
        if (this.listener != null) this.listener.setBalsaApplication(this);
    }

    /**
     * Get the routing engine used by this application
     * 
     * @return returns balsaRoutingEngine
     */
    public RouteEngine getRoutingEngine()
    {
        return routingEngine;
    }

    /**
     * Get the routers in the routing engine
     * 
     * @return returns List<Router>
     */
    public List<Router> getRouters()
    {
        return this.getRoutingEngine().getRouters();
    }

    /**
     * Add a router to this application
     * 
     * @param router
     *            returns void
     */
    public void router(Router router) throws BalsaException
    {
        if (router != null)
        {
            this.getRoutingEngine().router(router);
        }
    }

    /**
     * Get the session engine
     * 
     * @return returns SessionEngine
     */
    public SessionEngine getSessionEngine()
    {
        return this.sessionEngine;
    }

    /**
     * Set the session engine
     * 
     * @param engine
     *            returns void
     */
    public void sessionEngine(SessionEngine engine)
    {
        this.sessionEngine = engine;
        if (this.sessionEngine != null) this.sessionEngine.setBalsaApplication(this);
    }

    /**
     * Get the view engine
     * 
     * @return returns ViewEngine
     */
    public ViewEngine getViewEngine()
    {
        return this.viewEngine;
    }

    /**
     * Set the view engine
     * 
     * @param engine
     *            returns void
     */
    public void viewEngine(ViewEngine engine)
    {
        this.viewEngine = engine;
        if (this.viewEngine != null) this.viewEngine.setBalsaApplication(this);
    }
    
    
    
    public List<File> getViewPath()
    {
        return Collections.unmodifiableList(this.viewPath);
    }
    
    public void viewPath(File path)
    {
        this.viewPath.add(path);
    }
    
    public String[] templates()
    {
        return this.templates;
    }
    
    public String[] getTemplates()
    {
        return this.templates;
    }
    
    /**
     * Get the engine responsible for handling authentication and authorisation
     */
    public SecurityEngine getSecurityEngine()
    {
        return this.securityEngine;
    }
    
    /**
     * Set the engine responsible for handling authentication and authorisation
     */
    public void securityEngine(SecurityEngine securityEngine)
    {
        this.securityEngine = securityEngine;
        if (securityEngine != null) securityEngine.setBalsaApplication(this);
    }
    
    /**
     * Get the engine responsible for handling all public resource URLs
     */
    public PublicResourceEngine getPublicResourceEngine()
    {
        return this.publicResourceEngine;
    }
    
    /**
     * Set the engine responsible for handling all public resource URLs
     */
    public void publicResourceEngine(PublicResourceEngine publicResourceEngine)
    {
        this.publicResourceEngine = publicResourceEngine;
        if (publicResourceEngine != null) publicResourceEngine.setBalsaApplication(this);
    }

    /**
     * Get the filters this application is using
     * 
     * @return the List<BalsaFilter> of filters
     */
    public List<BalsaFilter> filters()
    {
        return this.filters;
    }

    /**
     * Add a filter to this application
     * 
     * @param filter
     *            the BalsaFilter to add
     */
    public void filter(BalsaFilter filter)
    {
        this.filters.add(filter);
    }

    /**
     * Remove all filters from this application
     */
    public void clearFilters()
    {
        this.filters.clear();
    }

    /*
     * Beans
     */

    /**
     * Register a model
     * 
     * @param model
     *            returns void
     */
    @SuppressWarnings("unchecked")
    public <E> void model(Class<E> bean) throws BalsaException
    {
        if (bean != null)
        {
            String name = bean.getName();
            if (!this.models.containsKey(name))
            {
                Pooled pooled = bean.getAnnotation(Pooled.class);
                if (pooled != null)
                {
                    if (BeanFactory.class.isAssignableFrom(pooled.value()))
                    {
                        try
                        {
                            this.models.put(name, new PooledBean<E>(bean, (BeanFactory<E>) pooled.value().newInstance()));
                            logger.info("Registered pooled bean " + name + " with factory " + pooled.value().getName());
                        }
                        catch (Exception e)
                        {
                            throw new BalsaException("Could not create the bean factory: " + pooled.value().getName(), e);
                        }
                    }
                    else
                    {
                        this.models.put(name, new PooledBean<E>(bean));
                        logger.info("Registered pooled bean " + name + " with default factory");
                    }
                }
                else
                {
                    this.models.put(name, new NonPooledBean<E>(bean));
                    logger.info("Registered non pooled bean " + name);
                }
            }
        }
    }

    /**
     * Register a model provider
     * 
     * @param provider
     *            returns void
     */
    public <E> void model(BeanProvider<E> provider)
    {
        if (provider != null)
        {
            String name = provider.getBeanClass().getName();
            if (!this.models.containsKey(name))
            {
                this.models.put(name, provider);
                logger.info("Registered pooled bean " + name + " with provider " + provider.getClass());
            }
        }
    }

    /**
     * Get the provider for the given model
     * 
     * @param type
     * @return returns BeanProvider<E>
     */
    @SuppressWarnings("unchecked")
    public <E> BeanProvider<E> provider(Class<E> type)
    {
        if (type == null) return null;
        return (BeanProvider<E>) this.models.get(type.getName());
    }

    /**
     * Register the given actions
     * 
     * returns void
     */
    public void action(Object action)
    {
        // scan the given class for actions
        for (ActionHandler handler : MethodActionHandler.findActionHandlers(action))
        {
            this.action(handler);
        }
    }

    /**
     * Register the given action handler
     * 
     * @param action
     *            returns void
     */
    public void action(ActionHandler action)
    {
        if (action != null)
        {
            if (this.logger.isTraceEnabled()) logger.trace("Registered action handler " + action.getName() + " => " + action);
            this.actions.put(action.getName(), action);
        }
    }

    /**
     * Get the action handler for the given name
     * 
     * @param name
     *            the action name
     * @return returns ActionHandler
     */
    public ActionHandler action(String name)
    {
        return this.actions.get(name);
    }

    /**
     * Create a model of the given type
     * 
     * @param type
     *            the model class
     * @return returns Object
     */
    @SuppressWarnings("unchecked")
    public <E> E activateModel(Class<E> type)
    {
        BeanProvider<E> provider = (BeanProvider<E>) this.models.get(type.getName());
        if (provider != null) return provider.activate();
        return null;
    }

    /**
     * Return the the model to the pool
     * 
     * @param object
     *            the model returns void
     */
    @SuppressWarnings("unchecked")
    public void deactivateModel(Object bean)
    {
        if (bean != null)
        {
            BeanProvider<Object> provider = (BeanProvider<Object>) this.models.get(bean.getClass().getName());
            if (provider != null) provider.deactivate(bean);
        }
    }
    
    /**
     * Get the Express extensions for this Balsa application
     * @return
     */
    public ExpressExtensionRegistry expressExtensions()
    {
        return this.expressExtensions;
    }

    public ExpressExtensionRegistry function(String name, Class<? extends Function> functionClass)
    {
        return expressExtensions.addFunction(name, functionClass);
    }

    public ExpressExtensionRegistry decorator(String name, Class<?> entityType, Class<? extends Decorator> decoratorClass)
    {
        return expressExtensions.addDecorator(name, entityType, decoratorClass);
    }

    /*
     * Life cycle
     */

    /**
     * Setup the Balsa application
     * 
     * @throws Exception
     */
    protected abstract void setup() throws Exception;
    
    // startup hooks
    
    protected void startListener() throws Exception
    {
        logger.info("Starting listener.");
    }
    
    protected void startComplete() throws Exception
    {
        logger.info("Startup Complete.");
    }

    /**
     * Start the Balsa application
     * 
     * @throws Exception
     *             returns void
     */
    public final void start() throws Exception
    {
        // Setup the app
        this.setup();
        // Check we have stuff
        if (this.getListener() == null) throw new BalsaException("The Balsa application must have a listener");
        if (this.getViewEngine() == null) throw new BalsaException("The Balsa application must have a view engine");
        // Set the common args
        this.getListener().setPort(this.getIntArgument("port", BalsaListener.DEFAULT_PORT));
        this.getListener().setPoolSize(this.getIntArgument("workers", BalsaListener.DEFAULT_POOL_SIZE));
        if (this.getSessionEngine() != null)
        {
            this.getSessionEngine().setPoolSize(this.getIntArgument("workers", BalsaListener.DEFAULT_POOL_SIZE));
            this.getSessionEngine().setSessionLifetime(this.getIntArgument("session-lifetime", SessionEngine.DEFAULT_SESSION_LIFETIME));
        }
        this.viewPath(new File(this.getArgument("views", FileViewSource.DEFAULT_VIEW_PATH)));
        // if in development mode
        if ("true".equalsIgnoreCase(this.getArgument("dev", "false")))
        {
            // disable view caching
            this.viewEngine.cacheOff();
        }
        // Construct the processor chain
        this.getListener().setProcessor(this.constructProcessingChain());
        // Start the session engine
        if (this.getSessionEngine() != null) this.getSessionEngine().start();
        // Start the view engine
        this.getViewEngine().start();
        // Start the listener
        this.getListener().start();
    }

    private BalsaProcessor constructProcessingChain()
    {
        return this.constructHeadFilterChain(this.constructFilterChain(this.filters(), this.constructTailFilterChain(new RouteProcessor(this.getRoutingEngine()))));
    }

    private BalsaProcessor constructHeadFilterChain(BalsaProcessor processor)
    {
        BalsaProcessor head = processor;
        head = new FilterProcessor(new SessionFilter(), head);
        return head;
    }

    private BalsaProcessor constructTailFilterChain(BalsaProcessor processor)
    {
        return processor;
    }

    private BalsaProcessor constructFilterChain(List<BalsaFilter> filters, BalsaProcessor processor)
    {
        BalsaProcessor head = processor;
        ListIterator<BalsaFilter> i = filters.listIterator();
        while (i.hasPrevious())
        {
            head = new FilterProcessor(i.previous(), head);
        }
        return head;
    }

    /**
     * Shutdown the Balsa application
     */
    protected void shutdown()
    {
    }

    /**
     * Stop the Balsa application
     */
    public final void stop()
    {
        this.getListener().stop();
        this.getViewEngine().stop();
        if (this.getSessionEngine() != null) this.getSessionEngine().stop();
        this.shutdown();
    }
}
