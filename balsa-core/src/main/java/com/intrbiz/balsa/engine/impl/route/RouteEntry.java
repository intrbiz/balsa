package com.intrbiz.balsa.engine.impl.route;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.engine.route.RouteExecutor;
import com.intrbiz.balsa.error.BalsaInternalError;
import com.intrbiz.balsa.listener.BalsaRequest;
import com.intrbiz.balsa.parameter.StringParameter;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

/**
 * An entry in the routing engine
 */
public class RouteEntry implements Comparable<RouteEntry>
{
    private final PrefixContainer prefix;

    private final Route route;

    private final RouteExecutor<?> executor;

    private final Timer requestDuration;

    public RouteEntry(PrefixContainer prefix, Route route, RouteExecutor<?> executor) throws BalsaInternalError
    {
        super();
        this.prefix = prefix;
        this.route = route;
        this.executor = executor;
        //
        this.requestDuration = Metrics.newTimer(new RouteMetricName(route, "request-duration"), TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
    }

    public PrefixContainer getPrefix()
    {
        return this.prefix;
    }

    public Route getRoute()
    {
        return this.route;
    }

    public boolean isExceptionHandler()
    {
        return this.route.isExceptionHandler();
    }

    public boolean match(BalsaRequest request)
    {
        if ("ANY".equals(this.route.getMethod()) || this.route.getMethod().equals(request.getRequestMethod()))
        {
            Matcher m = this.route.getCompiledPattern().pattern.matcher(request.getPathInfo());
            if (m.matches())
            {
                // extract parameters
                if (this.route.getCompiledPattern().as.length > 0)
                {
                    for (int i = 0; i < m.groupCount() && i < this.route.getCompiledPattern().as.length; i++)
                    {
                        request.addParameter(new StringParameter(this.route.getCompiledPattern().as[i], m.group(i + 1)));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean matchException(BalsaRequest request, Throwable t)
    {
        if ("ANY".equals(this.route.getMethod()) || this.route.getMethod().equals(request.getRequestMethod()))
        {
            Matcher m = this.route.getCompiledPattern().pattern.matcher(request.getPathInfo());
            if (m.matches())
            {
                // do we have an exception match
                for (Class<? extends Throwable> exception : this.route.getExceptions())
                {
                    if (exception.isInstance(t)) return true;
                }
            }
        }
        return false;
    }

    public void execute(BalsaContext context) throws Throwable
    {
        if (this.route.isExceptionHandler()) throw new IllegalAccessException("An exception handler can only be used to handle errors!");
        TimerContext tctx = this.requestDuration.time();
        try
        {
            this.route.getRouter().before();
            //
            if (this.executor != null) this.executor.execute(context);
            //
            this.route.getRouter().after();
        }
        finally
        {
            tctx.stop();
        }
    }

    public void executeException(BalsaContext context) throws Throwable
    {
        if (!this.route.isExceptionHandler()) throw new IllegalAccessException("An exception handler can only be used to handle errors!");
        TimerContext tctx = this.requestDuration.time();
        try
        {
            if (this.executor != null) this.executor.execute(context);
        }
        finally
        {
            tctx.stop();
        }
    }

    public String toString()
    {
        return "RouteHandler: " + this.route.getMethod() + " " + this.route.getCompiledPattern() + " => " + (this.executor == null ? "void" : this.executor.getClass().getCanonicalName());
    }

    protected String stripTrailingSlash(String s)
    {
        if ("/".equals(s)) return "";
        if (s.endsWith("/")) { return s.substring(0, s.length() - 1); }
        return s;
    }

    protected String ensureStartingSlash(String s)
    {
        if (!s.startsWith("/")) return "/" + s;
        return s;
    }

    @Override
    public int compareTo(RouteEntry o)
    {
        return this.route.compareTo(o.route);
    }
}
