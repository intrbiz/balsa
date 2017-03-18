package com.intrbiz.balsa;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.intrbiz.balsa.bean.BeanProvider;
import com.intrbiz.balsa.bean.impl.SimpleBeanProvider;
import com.intrbiz.balsa.engine.PublicResourceEngine;
import com.intrbiz.balsa.engine.RouteEngine;
import com.intrbiz.balsa.engine.SecurityEngine;
import com.intrbiz.balsa.engine.SessionEngine;
import com.intrbiz.balsa.engine.TaskEngine;
import com.intrbiz.balsa.engine.ViewEngine;
import com.intrbiz.balsa.engine.impl.publicresource.PublicResourceEngineImpl;
import com.intrbiz.balsa.engine.impl.route.RouteEngineImpl;
import com.intrbiz.balsa.engine.impl.security.SecurityEngineImpl;
import com.intrbiz.balsa.engine.impl.session.SimpleSessionEngine;
import com.intrbiz.balsa.engine.impl.task.TaskEngineImpl;
import com.intrbiz.balsa.engine.impl.view.BalsaViewEngineImpl;
import com.intrbiz.balsa.engine.impl.view.FileViewSource;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.engine.security.method.AuthenticationMethod;
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
import com.intrbiz.balsa.listener.filter.PublicResourceFilter;
import com.intrbiz.balsa.listener.filter.SessionFilter;
import com.intrbiz.balsa.listener.http.BalsaHTTPListener;
import com.intrbiz.balsa.listener.processor.FilterProcessor;
import com.intrbiz.balsa.listener.processor.RouteProcessor;
import com.intrbiz.balsa.listener.scgi.BalsaSCGIListener;
import com.intrbiz.express.ExpressExtensionRegistry;
import com.intrbiz.express.action.ActionHandler;
import com.intrbiz.express.action.MethodActionHandler;
import com.intrbiz.express.functions.TextFunctionRegistry;
import com.intrbiz.express.operator.Decorator;
import com.intrbiz.express.operator.Function;

/**
 * A balsa Application
 */
public abstract class BalsaApplication
{
    private static BalsaApplication INSTANCE = null;
    
    public static final BalsaApplication getInstance()
    {
        return INSTANCE;
    }
    
    private Logger logger = Logger.getLogger(BalsaApplication.class);

    /**
     * Bean providers
     */
    private final ConcurrentMap<String, BeanProvider<?>> models = new ConcurrentHashMap<String, BeanProvider<?>>();
    
    /**
     * Actions
     */
    private final ConcurrentMap<String, ActionHandler> actions = new ConcurrentHashMap<String, ActionHandler>();

    /**
     * The listener
     */
    private final ConcurrentMap<String, BalsaListener> listeners = new ConcurrentHashMap<String, BalsaListener>();

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
     * The task engine for executing long running jobs
     */
    private TaskEngine taskEngine;
    
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
    private final List<BalsaFilter> filters = new CopyOnWriteArrayList<BalsaFilter>();
    

    /**
     * The ExpressExtensionRegistry where Balsa stores its extensions
     */
    private final ExpressExtensionRegistry expressExtensions = new ExpressExtensionRegistry("balsa")
                                                               .addSubRegistry(ExpressExtensionRegistry.getDefaultRegistry())
                                                               .addSubRegistry(new TextFunctionRegistry());
    
    private String[] templates = new String[0];
    
    private String env = getApplicationEnv();
    
    private String instanceName = getApplicationInstanceName(this.getClass());

    public BalsaApplication()
    {
        super();
        // set the application instance
        INSTANCE = this;
        /* Default Functions */
        // immutable
        this.expressExtensions.addFunction(new BalsaFunction());
        this.expressExtensions.addFunction(new PathInfoFunction());
        this.expressExtensions.addFunction(new TitleFunction());
        this.expressExtensions.addFunction(new RequestTokenFunction());
        this.expressExtensions.addFunction("path", PathFunction.class);
        this.expressExtensions.addFunction("public", PublicFunction.class);
        this.expressExtensions.addFunction("access_token_for_url", RequestPathTokenFunction.class);
    }

    /**
     * Get the listener for this application
     * 
     * @return returns BalsaListener
     */
    public Collection<BalsaListener> getListeners()
    {
        return this.listeners.values();
    }

    /**
     * Setup the listener which will be used to process requests from the web server
     * 
     * @param listener
     *            The listener to use
     */
    public void listener(BalsaListener listener)
    {
        if (listener != null)
        {
            listener.setBalsaApplication(this);
            this.listeners.put(listener.getListenerType(), listener);
        }
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
    public List<Router<?>> getRouters()
    {
        return this.getRoutingEngine().getRouters();
    }

    /**
     * Add a router to this application
     * 
     * @param router
     *            returns void
     */
    public void router(Router<?> router) throws Exception
    {
        if (router != null)
        {
            router.setup();
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
    
    /**
     * The view paths
     */
    public List<File> getViewPath()
    {
        return Collections.unmodifiableList(this.viewPath);
    }
    
    /**
     * Add a view path
     */
    public void viewPath(File path)
    {
        this.viewPath.add(path);
    }
    
    /**
     * The application templates
     */
    public String[] templates()
    {
        return this.templates;
    }
    
    /**
     * The application templates
     */
    public String[] getTemplates()
    {
        return this.templates;
    }
    
    /**
     * Add an application template
     */
    public void template(String template)
    {
        String[] s = new String[this.templates.length + 1];
        System.arraycopy(this.templates, 0, s, 0, this.templates.length);
        s[s.length] = template;
        this.templates = s;
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
     * Get the named authentication method
     */
    public <T extends AuthenticationMethod<?>> T getAuthenticationMethod(String name)
    {
        return this.getSecurityEngine().getAuthenticationMethod(name);
    }
    
    public void authenticationMethod(AuthenticationMethod<?> authenticationMethod)
    {
        if (this.securityEngine == null)
            throw new IllegalStateException("Cannot register authentication method as no security engine is currently setup");
        this.securityEngine.registerAuthenticationMethod(authenticationMethod);
    }
    
    /**
     * Get the engine responsible for executing long running jobs in the background
     */
    public TaskEngine getTaskEngine()
    {
        return this.taskEngine;
    }
    
    /**
     * Set the engine responsible for executing long running jobs in the background
     */
    public void taskEngine(TaskEngine taskEngine)
    {
        this.taskEngine = taskEngine;
        if (taskEngine != null) taskEngine.setBalsaApplication(this);
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
    public <E> BeanProvider<E> model(Class<E> bean) throws BalsaException
    {
        if (bean != null)
        {
            String name = bean.getName();
            BeanProvider<E> provider = (BeanProvider<E>) this.models.get(name);
            if (provider == null)
            {
                provider = new SimpleBeanProvider<E>(bean);
                logger.info("Registering bean " + name);
                this.models.put(name, provider);
            }
            return provider;
        }
        return null;
    }

    /**
     * Register a model provider
     * 
     * @param provider
     *            returns void
     */
    public <E> BeanProvider<E> model(BeanProvider<E> provider)
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
        return provider;
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
    public <E> E createModel(Class<E> type)
    {
        BeanProvider<E> provider = this.model(type);
        return provider.create();
    }

    /**
     * Return the the model to the pool
     * 
     * @param object
     *            the model returns void
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void destroyModel(Object bean)
    {
        if (bean != null)
        {
            BeanProvider<?> provider = this.model(bean.getClass());
            ((BeanProvider) provider).destroy(bean);
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
    
    public ExpressExtensionRegistry immutableFunction(Function immutableFunction)
    {
        return expressExtensions.addFunction(immutableFunction);
    }

    public ExpressExtensionRegistry function(String name, Class<? extends Function> functionClass)
    {
        return expressExtensions.addFunction(name, functionClass);
    }

    public ExpressExtensionRegistry decorator(String name, Class<?> entityType, Class<? extends Decorator> decoratorClass)
    {
        return expressExtensions.addDecorator(name, entityType, decoratorClass);
    }
    
    public String getEnv()
    {
        return this.env;
    }
    
    public boolean isDevEnv()
    {
        return "dev".equals(this.getEnv());
    }
    
    public boolean isTestEnv()
    {
        return "test".equals(this.getEnv());
    }
    
    public boolean isProdEnv()
    {
        return "prod".equals(this.getEnv());
    }

    /*
     * Life cycle
     */

    /**
     * See:
     * - setupEngines()
     * - setupFunctions()
     * - setupActions()
     * - setupRouters()
     * @throws Exception
     */
    protected void setup() throws Exception
    {
        // setup application engines
        this.setupEngines();
        // setup the application functions
        this.setupFunctions();
        // setup the application actions
        this.setupActions();
        // setup the application routers
        this.setupRouters();
    }
    
    /**
     * Setup the application engines
     * @throws Exception
     */
    protected abstract void setupEngines() throws Exception;
    
    /**
     * Setup the application Express functions
     * @throws Exception
     */
    protected abstract void setupFunctions() throws Exception;
    
    /**
     * Setup the application actions
     * @throws Exception
     */
    protected abstract void setupActions() throws Exception;
    
    /**
     * Setup the application routers
     * @throws Exception
     */
    protected abstract void setupRouters() throws Exception;

    /**
     * Start the Balsa application
     * 
     * @throws Exception
     *             returns void
     */
    public final void start() throws Exception
    {
        // configure logging
        this.configureLogging();
        // defaults
        this.setupDefaultEngines();
        this.setupDefaultListeners();
        // Setup the app
        this.setup();
        // Check we have stuff
        if (this.getListeners().isEmpty()) throw new BalsaException("The Balsa application must have a listener");
        if (this.getViewEngine() == null) throw new BalsaException("The Balsa application must have a view engine");
        // configure the listeners
        for (BalsaListener listener : this.getListeners())
        {
            listener.setPort(this.getListenerPort(listener.getListenerType(), listener.getDefaultPort()));
            listener.setPoolSize(this.getListenerThreadCount(listener.getListenerType(), BalsaListener.DEFAULT_POOL_SIZE));
        }
        // configure the session engine
        if (this.getSessionEngine() != null)
        {
            this.getSessionEngine().setSessionLifetime(this.getSessionLifetime(SessionEngine.DEFAULT_SESSION_LIFETIME));
        }
        // settings based on environment
        if (this.isDevEnv())
        {
            // development settings
            this.viewPath(new File(getViewPath(FileViewSource.DEV_VIEW_PATH)));
            this.getViewEngine().cacheOff();
        }
        else if (this.isTestEnv())
        {
            // test settings
            this.viewPath(new File(getViewPath(FileViewSource.TEST_VIEW_PATH)));
        }
        else if (this.isProdEnv())
        {
            // production settings
            this.viewPath(new File(getViewPath(FileViewSource.PROD_VIEW_PATH)));
        }
        // Construct the processor chain
        BalsaProcessor proc = this.constructProcessingChain();
        logger.debug("Balsa processing chain: " + proc);
        for (BalsaListener listener : this.getListeners())
        {
            listener.setProcessor(proc);
        }
        // Start the task engine
        if (this.getTaskEngine() != null) this.getTaskEngine().start();
        // Start the session engine
        if (this.getSessionEngine() != null) this.getSessionEngine().start();
        // Start the view engine
        this.getViewEngine().start();
        // Application specific start up
        this.startApplication();
        // Start the listeners
        for (BalsaListener listener : this.getListeners())
        {
            logger.info("Starting listener: " + listener.getEngineName() + " on port " + listener.getPort());
            listener.start();
        }
    }
    
    // config resolvers
    
    protected int getListenerPort(String listenerType, int defaultPort)
    {
        return Integer.getInteger("balsa." + listenerType + ".port", defaultPort);
    }
    
    protected int getListenerThreadCount(String listenerType, int defaultThreadCount)
    {
        return Integer.getInteger("balsa." + listenerType + ".workers", Integer.getInteger("balsa.workers", defaultThreadCount));
    }
    
    protected int getSessionLifetime(int defaultSessionLifetime)
    {
        return Integer.getInteger("balsa.session-lifetime", defaultSessionLifetime);
    }
    
    protected String getViewPath(String defaultViewPath)
    {
        return System.getProperty("balsa.view.path", defaultViewPath);
    }
    
    /**
     * Any application specific startup actions, before the listeners are started
     * @throws Exception
     */
    protected abstract void startApplication() throws Exception;

    protected BalsaProcessor constructProcessingChain()
    {
        return this.constructHeadFilterChain(this.constructFilterChain(this.filters(), this.constructTailFilterChain(new RouteProcessor(this.getRoutingEngine()))));
    }

    protected BalsaProcessor constructHeadFilterChain(BalsaProcessor processor)
    {
        BalsaProcessor head = processor;
        head = new FilterProcessor(new SessionFilter(), head);
        return head;
    }

    protected BalsaProcessor constructTailFilterChain(BalsaProcessor processor)
    {
        return processor;
    }

    protected BalsaProcessor constructFilterChain(List<BalsaFilter> filters, BalsaProcessor processor)
    {
        BalsaProcessor head = processor;
        ListIterator<BalsaFilter> i = filters.listIterator(filters.size());
        while (i.hasPrevious())
        {
            head = new FilterProcessor(i.previous(), head);
        }
        return head;
    }
    
    protected void configureLogging()
    {
        String logging = System.getProperty("balsa.logging", "console");
        if ("console".equals(logging))
        {
            // configure logging to terminal
            BasicConfigurator.configure();
            Logger.getRootLogger().setLevel(Level.toLevel(System.getProperty("balsa.logging.level", "trace").toUpperCase()));
        }
        else
        {
            // configure from file
            PropertyConfigurator.configure(new File(logging).getAbsolutePath());
        }
    }
    
    protected void setupDefaultEngines() throws Exception
    {
        /* Default Engines */
        this.sessionEngine(new SimpleSessionEngine());
        this.viewEngine(new BalsaViewEngineImpl());
        this.securityEngine(new SecurityEngineImpl());
        this.publicResourceEngine(new PublicResourceEngineImpl());
        this.taskEngine(new TaskEngineImpl());
    }
    
    protected void setupDefaultListeners() throws Exception
    {
        /* Default Listeners */
        this.listener(new BalsaSCGIListener());
        // dev env addds HTTP listener
        if (this.isDevEnv() && Boolean.parseBoolean(System.getProperty("balsa.http", "true")))
        {
            this.listener(new BalsaHTTPListener());
            filter(new PublicResourceFilter(new File(System.getProperty("balsa.public.path", PublicResourceFilter.DEV_PUBLIC_PATH))));
        }
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
        for (BalsaListener listener : this.getListeners())
        {
            listener.stop();   
        }
        this.getViewEngine().stop();
        if (this.getSessionEngine() != null) this.getSessionEngine().stop();
        this.shutdown();
    }
    
    public String getInstanceName()
    {
        return this.instanceName;
    }
    
    public static String getApplicationInstanceName(Class<? extends BalsaApplication> applicationClass)
    {
        return System.getProperty("balsa.instance.name", applicationClass.getSimpleName().toLowerCase() + "." + getApplicationEnv());
    }
    
    public static String getApplicationEnv()
    {
        return System.getProperty("balsa.env", "dev");
    }
}
