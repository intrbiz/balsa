package com.intrbiz.balsa;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.bean.BeanFactory;
import com.intrbiz.balsa.bean.BeanProvider;
import com.intrbiz.balsa.bean.impl.NonPooledBean;
import com.intrbiz.balsa.bean.impl.PooledBean;
import com.intrbiz.balsa.engine.RouteEngine;
import com.intrbiz.balsa.engine.SessionEngine;
import com.intrbiz.balsa.engine.ViewEngine;
import com.intrbiz.balsa.engine.route.RouteExecutorFactory;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.engine.route.impl.RouteEngineImpl;
import com.intrbiz.balsa.engine.route.impl.executor.JSONExecutor;
import com.intrbiz.balsa.engine.route.impl.executor.JSONInExecutor;
import com.intrbiz.balsa.engine.route.impl.executor.ParameterExecutor;
import com.intrbiz.balsa.engine.route.impl.executor.VoidExecutor;
import com.intrbiz.balsa.engine.session.impl.SimpleSessionEngine;
import com.intrbiz.balsa.engine.view.impl.BalsaViewEngine;
import com.intrbiz.balsa.event.BalsaEventListener;
import com.intrbiz.balsa.event.BalsaSessionEvent;
import com.intrbiz.balsa.listener.BalsaListener;
import com.intrbiz.balsa.listener.BalsaMiddleware;
import com.intrbiz.balsa.listener.BalsaProcessor;
import com.intrbiz.balsa.listener.middleware.CookieMiddleware;
import com.intrbiz.balsa.listener.middleware.JSONBodyMiddleware;
import com.intrbiz.balsa.listener.middleware.LoggingMiddleware;
import com.intrbiz.balsa.listener.middleware.MiddlewareProcessor;
import com.intrbiz.balsa.listener.middleware.QueryStringMiddleware;
import com.intrbiz.balsa.listener.middleware.SessionMiddleware;
import com.intrbiz.balsa.listener.processor.RouteProcessor;
import com.intrbiz.balsa.listener.scgi.SCGIListener;
import com.intrbiz.balsa.view.loader.FileLoader;
import com.intrbiz.express.action.ActionHandler;
import com.intrbiz.express.action.MethodActionHandler;
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
     * Middleware defined by the application
     */
    private final List<BalsaMiddleware> middleware = new LinkedList<BalsaMiddleware>();

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

    public BalsaApplication()
    {
        super();
        /* defaults */
        this.listener(new SCGIListener());
        this.executor(new VoidExecutor());
        this.executor(new ParameterExecutor());
        this.executor(new JSONExecutor());
        this.executor(new JSONInExecutor());
        this.sessionEngine(new SimpleSessionEngine());
        this.viewEngine(new BalsaViewEngine());
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
     * Get the middleware used in this application
     * 
     * @return returns List<BalsaMiddleware>
     */
    public List<BalsaMiddleware> getMiddleware()
    {
        return middleware;
    }

    /**
     * Set the middleware of the application
     * 
     * @param middleware
     *            returns void
     */
    public void middleware(List<BalsaMiddleware> middleware)
    {
        this.middleware.clear();
        if (middleware != null)
        {
            for (BalsaMiddleware m : middleware)
            {
                this.middleware(m);
            }
        }
    }

    /**
     * Add middleware to the application
     * 
     * @param middleware
     *            returns void
     */
    public void middleware(BalsaMiddleware middleware)
    {
        this.middleware.add(middleware);
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
     * Add a route handler executor to the application
     * 
     * @param executor
     *            returns void
     */
    public void executor(RouteExecutorFactory executor)
    {
        this.getRoutingEngine().executor(executor);
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
     * Listen to events from the session engine
     * @param listener
     */
    public void sessionEventListener(BalsaEventListener<BalsaSessionEvent> listener)
    {
        this.getSessionEngine().listen(listener);
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

    /**
     * Add a template to be used by this application
     * 
     * @param template
     *            returns void
     */
    public void template(String template)
    {
        if (this.viewEngine != null) this.viewEngine.template(template);
    }

    /**
     * Set the templates used by this application
     * 
     * @param templates
     *            returns void@param name
     */
    public void templates(String[] templates)
    {
        if (this.viewEngine != null) this.viewEngine.templates(templates);
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
     * @param provider
     * returns void
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
     * @param type
     * @return
     * returns BeanProvider<E>
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
     * @param action
     * returns void
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
     * @param name the action name
     * @return
     * returns ActionHandler
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

    /*
     * Life cycle
     */

    /**
     * Setup the Balsa application
     * 
     * @throws BalsaException
     */
    protected abstract void setup() throws BalsaException;

    /**
     * Start the Balsa application
     * 
     * @throws BalsaException
     *             returns void
     */
    public final void start() throws BalsaException
    {
        // Setup the app
        this.setup();
        // Check we have stuff
        if (this.getListener() == null) throw new BalsaException("The Balsa application must have a listener");
        if (this.getSessionEngine() == null) throw new BalsaException("The Balsa application must have a session engine");
        if (this.getViewEngine() == null) throw new BalsaException("The Balsa application must have a view engine");
        // Set the common args
        this.getListener().setPort(this.getIntArgument("port", BalsaListener.DEFAULT_PORT));
        this.getListener().setPoolSize(this.getIntArgument("workers", BalsaListener.DEFAULT_POOL_SIZE));
        this.getSessionEngine().setPoolSize(this.getIntArgument("workers", BalsaListener.DEFAULT_POOL_SIZE));
        this.getSessionEngine().setSessionLifetime(this.getIntArgument("session-lifetime", SessionEngine.DEFAULT_SESSION_LIFETIME));
        this.getViewEngine().base(new File(this.getArgument("views", FileLoader.DEFAULT_BASE)));
        // if in development mode
        if ("true".equalsIgnoreCase(this.getArgument("dev", "false")))
        {
            // disable view caching
            this.viewEngine.cacheOff();
        }
        // Construct the processor chain
        this.getListener().setProcessor(this.constructProcessingChain());
        // Start the session engine
        this.getSessionEngine().start();
        // Start the view engine
        this.getViewEngine().start();
        // Start the listener
        this.getListener().start();
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
        this.getSessionEngine().stop();
        this.shutdown();
    }

    private BalsaProcessor constructProcessingChain()
    {
        return this.constructHeadMiddlewareChain(this.constructMiddlewareChain(this.getMiddleware(), this.constructTailMiddlewareChain(new RouteProcessor(this.getRoutingEngine()))));
    }

    private BalsaProcessor constructHeadMiddlewareChain(BalsaProcessor processor)
    {
        BalsaProcessor head = processor;
        head = new MiddlewareProcessor(new LoggingMiddleware(), head);
        head = new MiddlewareProcessor(new SessionMiddleware(), head);
        head = new MiddlewareProcessor(new CookieMiddleware(), head);
        head = new MiddlewareProcessor(new JSONBodyMiddleware(), head);
        head = new MiddlewareProcessor(new QueryStringMiddleware(), head);
        return head;
    }

    private BalsaProcessor constructTailMiddlewareChain(BalsaProcessor processor)
    {
        return processor;
    }

    private BalsaProcessor constructMiddlewareChain(List<BalsaMiddleware> middleware, BalsaProcessor processor)
    {
        BalsaProcessor head = processor;
        ListIterator<BalsaMiddleware> i = middleware.listIterator();
        while (i.hasPrevious())
        {
            head = new MiddlewareProcessor(i.previous(), head);
        }
        return head;
    }
}
