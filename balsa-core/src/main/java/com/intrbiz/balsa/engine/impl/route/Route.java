package com.intrbiz.balsa.engine.impl.route;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Catch;
import com.intrbiz.metadata.Delete;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.IsRoute;
import com.intrbiz.metadata.Order;
import com.intrbiz.metadata.Post;
import com.intrbiz.metadata.Put;

public class Route implements Comparable<Route>
{
    private final String prefix;

    private final String method;

    private final String pattern;

    private final boolean regex;

    private final String[] as;

    private final Method handler;

    private final Router<?> router;

    private CompiledPattern compiledPattern;

    private Class<? extends Throwable>[] exceptions = null;

    private int order = 0;

    public Route(String prefix, String method, String pattern, boolean regex, String[] as, Method handler, Router<?> router)
    {
        super();
        this.prefix = prefix;
        this.method = method.toUpperCase();
        this.pattern = pattern;
        this.regex = regex;
        this.as = as;
        this.handler = handler;
        this.router = router;
    }

    public String prefix()
    {
        return this.prefix;
    }
    
    public String getPrefix()
    {
        return this.prefix;
    }

    public String getMethod()
    {
        return method;
    }

    public String getPattern()
    {
        return pattern;
    }

    public boolean isRegex()
    {
        return regex;
    }

    public String[] getAs()
    {
        return as;
    }

    public Method getHandler()
    {
        return handler;
    }

    public Router<?> getRouter()
    {
        return this.router;
    }

    public boolean isExceptionHandler()
    {
        return this.exceptions != null;
    }

    public Class<? extends Throwable>[] getExceptions()
    {
        return this.exceptions;
    }

    public void exceptions(Catch exceptions)
    {
        this.exceptions = exceptions.value();
    }

    public CompiledPattern getCompiledPattern()
    {
        return compiledPattern;
    }

    public void setCompiledPattern(CompiledPattern compiledPattern)
    {
        this.compiledPattern = compiledPattern;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Route other = (Route) obj;
        if (method == null)
        {
            if (other.method != null) return false;
        }
        else if (!method.equals(other.method)) return false;
        if (pattern == null)
        {
            if (other.pattern != null) return false;
        }
        else if (!pattern.equals(other.pattern)) return false;
        return true;
    }

    public int getOrder()
    {
        return this.order;
    }

    public void setOrder(int order)
    {
        this.order = order;
    }

    @Override
    public int compareTo(Route o)
    {
        return ((o.isRegex() ? 5000 : 0) + (this.isExceptionHandler() ? 10000 : 0) + this.order) - ((o.isRegex() ? 5000 : 0) + (o.isExceptionHandler() ? 10000 : 0) + o.order);
    }

    public String toString()
    {
        return "Route: " + this.getMethod() + " " + this.getPattern() + " => " + this.getHandler();
    }

    public static List<Route> fromRouter(String prefix, Router<?> router)
    {
        List<Route> routes = new LinkedList<Route>();
        fromRouter(prefix, router, router.getClass(), routes);
        return routes;
    }

    private static void fromRouter(String prefix, Router<?> router, Class<?> cls, List<Route> routes)
    {
        for (Method m : cls.getDeclaredMethods())
        {
            Route r = fromMethod(prefix, router, m);
            if (r != null) routes.add(r);
        }
        //
        if (cls.getSuperclass() != null) fromRouter(prefix, router, cls.getSuperclass(), routes);
    }

    public static Route fromMethod(String prefix, Router<?> router, Method method)
    {
        if (!Modifier.isPublic(method.getModifiers())) return null;
        // get the route annotation
        Annotation a = getRouteAnnotation(method);
        if (a == null) return null;
        // load the RouteBuilder
        RouteBuilder rb = getRouteBuilder(a);
        if (rb == null) return null;
        // build the route
        Route r = rb.build(prefix, router, method, a);
        if (r == null) return null;
        // annotations across all routes
        // is it an error route
        Catch error = method.getAnnotation(Catch.class);
        if (error != null) r.exceptions(error);
        // is there an order
        Order order = method.getAnnotation(Order.class);
        if (order != null) r.setOrder(order.value());
        // compile the pattern
        r.setCompiledPattern(compilePattern(r.getPrefix(), r.getPattern(), r.isRegex(), r.getAs()));
        return r;
    }

    public static boolean isRoute(Method m)
    {
        return getRouteAnnotation(m) != null;
    }

    public static Annotation getRouteAnnotation(Method method)
    {
        for (Annotation a : method.getAnnotations())
        {
            if (a.annotationType().getAnnotation(IsRoute.class) != null) return a;
        }
        return null;
    }

    public static RouteBuilder getRouteBuilder(Method method)
    {
        Annotation a = getRouteAnnotation(method);
        if (a == null) return null;
        return getRouteBuilder(a);
    }

    public static RouteBuilder getRouteBuilder(Annotation a)
    {
        // get the is route annoation
        IsRoute ir = a.annotationType().getAnnotation(IsRoute.class);
        if (ir == null) return null;
        // load the builder
        try
        {
            return ir.value().newInstance();
        }
        catch (Exception e)
        {
            // eat
        }
        return null;
    }

    private static String stripTrailingSlash(String s)
    {
        if ("/".equals(s)) return "";
        if (s.endsWith("/")) { return s.substring(0, s.length() - 1); }
        return s;
    }

    private static String ensureStartingSlash(String s)
    {
        if (!s.startsWith("/")) return "/" + s;
        return s;
    }

    private static final Pattern PARAM_REGEX2 = Pattern.compile("((?<!\\\\)\\*\\*(?::[A-Za-z0-9_]+)?)|((?<![\\\\*])\\*(?::[A-Za-z0-9_]+)?)|((?<!\\\\)\\+\\+(?::[A-Za-z0-9_]+)?)|((?<![\\\\+])\\+(?::[A-Za-z0-9_]+)?)|((?<![*+\\\\]):[A-Za-z0-9_]+)");

    public static class CompiledPattern
    {
        public final Pattern pattern;

        public final String[] as;

        public CompiledPattern(Pattern pattern, String[] as)
        {
            this.pattern = pattern;
            this.as = as;
        }
        
        public String toString()
        {
            return this.pattern.toString();
        }
    }
    
    public static CompiledPattern compilePattern(String prefix, String pattern, boolean isRegex, String[] as)
    {
        return isRegex ? compileRegexPattern(prefix, pattern, as) : compileBalsaPattern(prefix, pattern);
    }
    
    public static CompiledPattern compileRegexPattern(String prefix, String pattern, String[] as)
    {
        return new CompiledPattern(Pattern.compile("\\A" + stripTrailingSlash(prefix) + ensureStartingSlash(pattern) + "\\z"), as);
    }

    public static CompiledPattern compileBalsaPattern(String prefix, String pattern)
    {
        List<String> as = new LinkedList<String>();
        StringBuilder buffer = new StringBuilder();
        Matcher m = PARAM_REGEX2.matcher(pattern);
        int start = 0;
        while (m.find())
        {
            String op = m.group();
            //
            buffer.append(pattern.substring(start, m.start()));
            if ("**".equals(op))
            {
                buffer.append(".*");
            }
            else if ("*".equals(op))
            {
                buffer.append("[^/]*");
            }
            else if ("++".equals(op))
            {
                buffer.append(".+");
            }
            else if ("+".equals(op))
            {
                buffer.append("[^/]+");
            }
            else if (op.startsWith("**"))
            {
                buffer.append("(.*)");
                as.add(op.substring(3));
            }
            else if (op.startsWith("*"))
            {
                buffer.append("([^/]*)");
                as.add(op.substring(2));
            }
            else if (op.startsWith("++"))
            {
                buffer.append("(.+)");
                as.add(op.substring(3));
            }
            else if (op.startsWith("+"))
            {
                buffer.append("([^/]+)");
                as.add(op.substring(2));
            }
            else if (op.startsWith(":"))
            {
                buffer.append("([^/]+)");
                as.add(op.substring(1));
            }
            start = m.end();
        }
        if (start <= pattern.length()) buffer.append(pattern.substring(start));
        // compile the pattern
        return new CompiledPattern(Pattern.compile("\\A" + stripTrailingSlash(prefix) + ensureStartingSlash(buffer.toString()) + "\\z"), as.toArray(new String[as.size()]));
    }

    /**
     * Build a route given an annotated method
     */
    public static interface RouteBuilder
    {
        Route build(String prefix, Router<?> router, Method method, Annotation a);
    }

    public static class AnyRouteBuilder implements RouteBuilder
    {
        @Override
        public Route build(String prefix, Router<?> router, Method method, Annotation a)
        {
            Any url = (Any) a;
            return new Route(prefix, "ANY", url.value(), url.regex(), url.as(), method, router);
        }
    }

    public static class GetRouteBuilder implements RouteBuilder
    {
        @Override
        public Route build(String prefix, Router<?> router, Method method, Annotation a)
        {
            Get url = (Get) a;
            return new Route(prefix, "GET", url.value(), url.regex(), url.as(), method, router);
        }
    }

    public static class PostRouteBuilder implements RouteBuilder
    {
        @Override
        public Route build(String prefix, Router<?> router, Method method, Annotation a)
        {
            Post url = (Post) a;
            return new Route(prefix, "POST", url.value(), url.regex(), url.as(), method, router);
        }
    }

    public static class PutRouteBuilder implements RouteBuilder
    {
        @Override
        public Route build(String prefix, Router<?> router, Method method, Annotation a)
        {
            Put url = (Put) a;
            return new Route(prefix, "Put", url.value(), url.regex(), url.as(), method, router);
        }
    }

    public static class DeleteRouteBuilder implements RouteBuilder
    {
        @Override
        public Route build(String prefix, Router<?> router, Method method, Annotation a)
        {
            Delete url = (Delete) a;
            return new Route(prefix, "DELETE", url.value(), url.regex(), url.as(), method, router);
        }
    }
}
