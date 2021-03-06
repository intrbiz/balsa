package com.intrbiz.balsa.engine.impl.route;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.RouteEngine;
import com.intrbiz.balsa.engine.impl.AbstractBalsaEngine;
import com.intrbiz.balsa.engine.impl.route.Route.Filter;
import com.intrbiz.balsa.engine.impl.route.exec.ExecBuilder;
import com.intrbiz.balsa.engine.route.RouteExecutor;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.BalsaInternalError;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.listener.BalsaRequest;

/**
 * The actual routing engine
 *
 */
public class RouteEngineImpl extends AbstractBalsaEngine implements RouteEngine
{
    private List<Router<?>> routers = new ArrayList<Router<?>>();

    private List<PrefixContainer> prefixes = new ArrayList<PrefixContainer>();

    private Logger logger = Logger.getLogger(RouteEngineImpl.class);
    
    private int totalRoutes = 0;

    public RouteEngineImpl()
    {
        super();
    }

    public String getEngineName()
    {
        return "Balsa-Route-Engine";
    }

    public List<Router<?>> getRouters()
    {
        return Collections.unmodifiableList(this.routers);
    }
    
    public int getTotalRoutes()
    {
        return this.totalRoutes;
    }

    public void router(Router<?> router) throws BalsaException
    {
        // extract all the meta information
        this.routers.add(router);
        // compile the routes
        this.compileRouter(router);
    }

    protected void compileRouter(Router<?> router) throws BalsaException
    {
        // get the prefix for the router
        String prefix = router.getPrefix();
        // create the container
        PrefixContainer container = this.createContainer(prefix);
        // compile the routes
        List<Route> routes = Route.fromRouter(prefix, router);
        for (Route route : routes)
        {
            // compile an executor
            try
            {
                ExecBuilder execBuilder = ExecBuilder.build(route);
                RouteExecutor<?> exec = execBuilder.executor();
                //
                container.registerRoute(new RouteEntry(container, route, exec));
                this.totalRoutes++;
            }
            catch (Exception e)
            {
                throw new BalsaException("Failed to build route executor for route: " + route, e);
            }
        }
        // print container
        if (logger.isInfoEnabled())
        {
            logger.info(container.getPrefix());
            for (RouteEntry routeHandler : container.getRoutes())
            {
                logger.info("\t" + routeHandler.getRoute().getMethod() + " " + routeHandler.getRoute().getCompiledPattern() + " ==> " + routeHandler.getRoute().getHandler() + " order " + routeHandler.getRoute().computeOrder());
            }
            logger.info("Total routes: " + this.totalRoutes);
        }
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
            for (RouteEntry route : prefix.getRoutes())
            {
                s.append("\t\t" + route + "\r\n");
            }
        }
        s.append("Total routes: " + this.totalRoutes);
        return s.toString();
    }

    public void route(BalsaContext context) throws Throwable
    {
        // ROUTE!
        BalsaRequest request = context.request();
        // try all prefixes
        for (PrefixContainer prefix : this.prefixes)
        {
            if (prefix.match(context, request))
            {
                RouteEntry handler = prefix.getHandler(context, request);
                if (handler != null)
                {
                    // before filter
                    for (PrefixContainer filterPrefix : this.prefixes)
                    {
                        if (filterPrefix.match(context, request))
                        {
                            List<RouteEntry> filters = filterPrefix.getFilters(context, request, Filter.BEFORE);
                            if (filters != null)
                            {
                                for (RouteEntry filter : filters)
                                {
                                    filter.execute(context);
                                }
                            }
                        }
                    }
                    // the route
                    handler.execute(context);
                    // before filter
                    for (PrefixContainer filterPrefix : this.prefixes)
                    {
                        if (filterPrefix.match(context, request))
                        {
                            List<RouteEntry> filters = filterPrefix.getFilters(context, request, Filter.AFTER);
                            if (filters != null)
                            {
                                for (RouteEntry filter : filters)
                                {
                                    filter.execute(context);
                                }
                            }
                        }
                    }
                    // all done
                    return;
                }
            }
        }
        throw new BalsaNotFound();
    }

    public void routeException(BalsaContext context, Throwable t) throws Throwable
    {
        // Abort the current response
        context.response().abortOnError(t);
        // Route the error
        BalsaRequest request = context.request();
        // try all prefixes
        for (PrefixContainer prefix : this.prefixes)
        {
            if (prefix.match(context, request))
            {
                RouteEntry handler = prefix.getExceptionHandler(context, request, t);
                if (handler != null)
                {
                    // before filter
                    for (PrefixContainer filterPrefix : this.prefixes)
                    {
                        if (filterPrefix.match(context, request))
                        {
                            List<RouteEntry> filters = filterPrefix.getExceptionFilters(context, request, Filter.BEFORE);
                            if (filters != null)
                            {
                                for (RouteEntry filter : filters)
                                {
                                    filter.executeException(context);
                                }
                            }
                        }
                    }
                    // the exception route
                    handler.executeException(context);
                    // after filter
                    for (PrefixContainer filterPrefix : this.prefixes)
                    {
                        if (filterPrefix.match(context, request))
                        {
                            List<RouteEntry> filters = filterPrefix.getExceptionFilters(context, request, Filter.AFTER);
                            if (filters != null)
                            {
                                for (RouteEntry filter : filters)
                                {
                                    filter.executeException(context);
                                }
                            }
                        }
                    }
                    // all done
                    return;
                }
            }
        }
        // We cannot handle the error captain
        throw new BalsaInternalError(t);
    }
    
    @Override
    public void start() throws BalsaException
    {
        try
        {
            // start all the routers
            for (Router<?> router : this.getRouters())
            {
                router.start();
            }
        }
        catch (Exception e)
        {
            throw new BalsaException("Failed to start router", e);
        }
    }
}
