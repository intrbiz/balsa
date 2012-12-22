package com.intrbiz.balsa.engine.route.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.engine.route.RouteExecutor;
import com.intrbiz.balsa.engine.route.RouteExecutorFactory;
import com.intrbiz.balsa.error.BalsaInternalError;
import com.intrbiz.balsa.listener.BalsaRequest;
import com.intrbiz.balsa.parameter.StringParameter;
import com.intrbiz.metadata.Executor;

public class RouteHandler implements Comparable<RouteHandler>
{
    public static final Pattern PARAM_REGEX = Pattern.compile(":[a-zA-Z0-9_]+");

    private final PrefixContainer prefix;

    private final Route route;

    private Pattern pattern;

    private String[] as;

    private RouteExecutor executor;

    public RouteHandler(PrefixContainer prefix, Route route) throws BalsaInternalError
    {
        super();
        this.prefix = prefix;
        this.route = route;
        this.compilePattern();
        this.compileExecutor();
    }

    public PrefixContainer getPrefix()
    {
        return this.prefix;
    }

    public Route getRoute()
    {
        return this.route;
    }

    public Pattern getPattern()
    {
        return pattern;
    }

    public String[] getAs()
    {
        return this.as;
    }
    
    public boolean isExceptionHandler()
    {
        return this.route.isExceptionHandler();
    }

    public boolean match(BalsaRequest request)
    {
        if ("ANY".equals(this.route.getMethod()) || this.route.getMethod().equals(request.getRequestMethod()))
        {
            Matcher m = this.pattern.matcher(request.getPathInfo());
            if (m.matches())
            {
                // extract parameters
                if (this.as.length > 0)
                {
                    for (int i = 0; i < m.groupCount() && i < this.as.length; i++)
                    {
                        request.addParameter(new StringParameter(this.as[i], m.group(i + 1)));
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
            Matcher m = this.pattern.matcher(request.getPathInfo());
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
        this.route.getRouter().before();
        //
        if (this.executor != null) this.executor.execute(context);
        //
        this.route.getRouter().after();
    }
    
    public void executeException(BalsaContext context) throws Throwable
    {
        if (this.executor != null) this.executor.execute(context);
    }

    public String toString()
    {
        return "RouteHandler: " + this.route.getMethod() + " " + this.pattern.pattern() + " => " + (this.executor == null ? "void" : this.executor.getClass().getSimpleName());
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

    protected void compilePattern()
    {
        if (this.route.isRegex())
        {
            this.pattern = Pattern.compile("\\A" + this.stripTrailingSlash(this.prefix.getPrefix()) + this.ensureStartingSlash(this.route.getPattern()) + "\\z");
            this.as = this.route.getAs();
        }
        else
        {
            String pattern = route.getPattern();
            // process any wildcards
            pattern = pattern.replace("*", "[^/]*");
            pattern = pattern.replace("+", "[^/]+");
            // process any URL parameters
            List<String> as = new LinkedList<String>();
            StringBuilder buffer = new StringBuilder();
            Matcher m = PARAM_REGEX.matcher(pattern);
            int start = 0;
            while (m.find())
            {
                buffer.append(pattern.substring(start, m.start()));
                buffer.append("([^/]+)");
                as.add(m.group().substring(1));
                start = m.end();
            }
            if (start <= pattern.length()) buffer.append(pattern.substring(start));
            // as
            this.as = as.toArray(new String[as.size()]);
            // compile the pattern
            this.pattern = Pattern.compile("\\A" + this.stripTrailingSlash(this.prefix.getPrefix()) + this.ensureStartingSlash(buffer.toString()) + "\\z");
        }
    }

    protected void compileExecutor() throws BalsaInternalError
    {
        // Check for specified executor
        Executor exec = this.getRoute().getHandler().getAnnotation(Executor.class);
        if (exec != null)
        {
            try
            {
                RouteExecutorFactory factory = exec.value().newInstance();
                this.executor = factory.compileExecutor(this.getRoute().getHandler(), this.as, this.getRoute().getRouter());
                return;
            }
            catch (Exception e)
            {
                throw new BalsaInternalError("Failed to find executor for route handler: " + this.getRoute().getHandler(), e);
            }
        }
        // Use registered executors
        for (RouteExecutorFactory executor : this.getPrefix().getEngine().getExecutors())
        {
            if (executor.match(this.getRoute().getHandler(), this.as, this.getRoute().getRouter()))
            {
                this.executor = executor.compileExecutor(this.getRoute().getHandler(), this.as, this.getRoute().getRouter());
                return;
            }
        }
        throw new BalsaInternalError("Failed to find executor for route handler: " + this.getRoute().getHandler());
    }

    @Override
    public int compareTo(RouteHandler o)
    {
        return this.route.compareTo(o.route);
    }
}
