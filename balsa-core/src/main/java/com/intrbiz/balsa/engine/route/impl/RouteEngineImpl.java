package com.intrbiz.balsa.engine.route.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.RouteEngine;
import com.intrbiz.balsa.engine.impl.AbstractBalsaEngine;
import com.intrbiz.balsa.engine.route.RouteExecutorFactory;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.BalsaInternalError;
import com.intrbiz.balsa.error.BalsaNotFound;
import com.intrbiz.balsa.listener.BalsaRequest;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Catch;
import com.intrbiz.metadata.Delete;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.Order;
import com.intrbiz.metadata.Post;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Put;

/**
 * The actual routing engine
 *
 */
public class RouteEngineImpl extends AbstractBalsaEngine implements RouteEngine
{    
    private List<Router> routers = new ArrayList<Router>();
    
    private List<PrefixContainer> prefixes = new ArrayList<PrefixContainer>();
    
    private List<RouteExecutorFactory> executors = new LinkedList<RouteExecutorFactory>();
    
    private Logger logger = Logger.getLogger(RouteEngineImpl.class);
    
    public RouteEngineImpl()
    {
        super();
    }
    
    public List<RouteExecutorFactory> getExecutors()
    {
        return this.executors;
    }
    
    public void executor(RouteExecutorFactory executor)
    {
        this.executors.add(executor);
    }
    
    public List<Router> getRouters()
    {
        return Collections.unmodifiableList(this.routers);
    }

    public void router(Router router) throws BalsaException
    {
        // extract all the meta information
        this.routers.add(router);
        // compile the routes
        this.compileRouter(router);
    }
    
    protected void compileRouter(Router router) throws BalsaException
    {
        // get the prefix for the router
        String prefix = getPrefix(router);
        // create the container
        PrefixContainer container = this.createContainer(prefix);
        // compile the routes
        List<Route> routes = getRoutes(router);
        for (Route route : routes)
        {
            container.registerRoute(new RouteHandler(container, route));
        }
        // print container
        logger.debug(container.getPrefix());
        for (RouteHandler routeHandler : container.getRoutes())
        {
            logger.debug("\t" + routeHandler.getRoute().getMethod() + " " + routeHandler.getPattern() + " ==> " + routeHandler.getRoute().getHandler());
        }
    }
    
    protected final String getPrefix(Router router)
    {
        Class<?> clazz = router.getClass();
        Prefix prefix = clazz.getAnnotation(Prefix.class);
        if (prefix == null) return "/";
        return prefix.value();
    }

    protected List<Route> getRoutes(Router router)
    {
        List<Route> routes = new LinkedList<Route>();
        Class<?> clazz = router.getClass();
        while (clazz != null)
        {
            this.getRoutesForClass(routes, clazz, router);
            clazz = clazz.getSuperclass();
        }
        Collections.sort(routes);
        return routes;
    }

    protected void getRoutesForClass(List<Route> routes, Class<?> clazz, Router router)
    {
        Method[] methods = clazz.getDeclaredMethods();
        if (methods != null)
        {
            for (Method method : methods)
            {
                if (Modifier.isPublic(method.getModifiers()))
                {
                    // check for each of the methods we support
                    Route route = getRouteForMethod(method, router);
                    if (route != null)
                    {
                        // is it an error route
                        Catch error = method.getAnnotation(Catch.class);
                        if (error != null) route.exceptions(error);
                        Order order = method.getAnnotation(Order.class);
                        if (order != null) route.setOrder(order.value());
                        routes.add(route);
                    }
                }
            }
        }
    }

    protected Route getRouteForMethod(Method method, Router router)
    {
        // shame java lacks inheritance in annotations
        if (method.getAnnotation(Any.class) != null)
        {
            Any url = method.getAnnotation(Any.class);
            return new Route("ANY", url.value(), url.regex(), url.as(), method, router);
        }
        else if (method.getAnnotation(Get.class) != null)
        {
            Get url = method.getAnnotation(Get.class);
            return new Route("GET", url.value(), url.regex(), url.as(), method, router);
        }
        else if (method.getAnnotation(Post.class) != null)
        {
            Post url = method.getAnnotation(Post.class);
            return new Route("POST", url.value(), url.regex(), url.as(), method, router);
        }
        else if (method.getAnnotation(Put.class) != null)
        {
            Put url = method.getAnnotation(Put.class);
            return new Route("PUT", url.value(), url.regex(), url.as(), method, router);
        }
        else if (method.getAnnotation(Delete.class) != null)
        {
            Delete url = method.getAnnotation(Delete.class);
            return new Route("DELETE", url.value(), url.regex(), url.as(), method, router);
        }
        return null;
    }
    
    protected PrefixContainer createContainer(String prefix)
    {
        for (PrefixContainer container : this.prefixes)
        {
            if (prefix.equals(container.getPrefix())) return container;
        }
        PrefixContainer container = new PrefixContainer(prefix, this);
        this.prefixes.add(container);
        Collections.sort(this.prefixes);
        return container;
    }
    
    public String toString()
    {
        StringBuilder s = new StringBuilder("RoutingEngine\r\n");
        for (PrefixContainer prefix : this.prefixes)
        {
            s.append("\t" + prefix.toString() + "\r\n");
            for (RouteHandler route : prefix.getRoutes())
            {
                s.append("\t\t" + route + "\r\n");
            }
        }
        return s.toString();
    }
    
    
    public void route(BalsaContext context) throws Throwable
    {
        // ROUTE!
        BalsaRequest request = context.getRequest();
        // find the prefix
        PrefixContainer prefix = null;
        for (Iterator<PrefixContainer> i = this.prefixes.iterator(); i.hasNext();)
        {
            prefix = i.next();
            if (prefix.match(request)) break;
        }
        if (prefix != null)
        {
            RouteHandler handler = prefix.getHandler(request);
            if (handler != null)
            {
                handler.execute(context);
                return;
            }
        }
        throw new BalsaNotFound();
    }
    
    public void routeException(BalsaContext context, Throwable t) throws Throwable
    {
        // Abort the current response
        context.getResponse().abortOnError(t);
        // Route the error
        BalsaRequest request = context.getRequest();
        // find the prefix
        PrefixContainer prefix = null;
        Iterator<PrefixContainer> i = this.prefixes.iterator();
        while (i.hasNext())
        {
            prefix = i.next();
            if (prefix.match(request)) break;
        }
        if (prefix != null)
        {
            RouteHandler handler = prefix.getExceptionHandler(request, t);
            if (handler != null)
            {
                handler.executeException(context);
                return;
            }
        }
        // We cannot handle the error captain
        throw new BalsaInternalError(t);
    }
}
