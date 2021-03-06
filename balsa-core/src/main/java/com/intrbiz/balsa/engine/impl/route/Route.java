package com.intrbiz.balsa.engine.impl.route;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.listener.BalsaRequest;
import com.intrbiz.metadata.After;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Before;
import com.intrbiz.metadata.Catch;
import com.intrbiz.metadata.Delete;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.IsRoute;
import com.intrbiz.metadata.IsRoutePredicate;
import com.intrbiz.metadata.Options;
import com.intrbiz.metadata.Order;
import com.intrbiz.metadata.Post;
import com.intrbiz.metadata.Put;

public class Route implements Comparable<Route>
{
    public enum Filter
    {
        BEFORE,
        AFTER
    }
    
    private final String prefix;

    private final String method;

    private final String pattern;

    private final boolean regex;

    private final String[] as;

    private final Method handler;

    private final Router<?> router;
    
    private final Filter filter;

    private CompiledPattern compiledPattern;
    
    private RoutePredicate[] predicates = null;

    private Class<? extends Throwable>[] exceptions = null;

    private int order = 0;

    public Route(String prefix, String method, String pattern, boolean regex, String[] as, Method handler, Router<?> router, Filter filter)
    {
        super();
        this.prefix = prefix;
        this.method = method.toUpperCase();
        this.pattern = pattern;
        this.regex = regex;
        this.as = as;
        this.handler = handler;
        this.router = router;
        this.filter = filter;
    }
    
    public boolean isFilter()
    {
        return this.filter != null;
    }
    
    public Filter getFilter()
    {
        return this.filter;
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

    @SuppressWarnings("unchecked")
    public void exceptions(Collection<Class<? extends Throwable>> exceptions)
    {
        this.exceptions = (exceptions == null || exceptions.isEmpty()) ? null : exceptions.toArray(new Class[exceptions.size()]);
    }

    public CompiledPattern getCompiledPattern()
    {
        return compiledPattern;
    }

    public void setCompiledPattern(CompiledPattern compiledPattern)
    {
        this.compiledPattern = compiledPattern;
    }
    
    public RoutePredicate[] getPredicates()
    {
        return this.predicates;
    }
    
    public boolean hasPredicates()
    {
        return this.predicates != null;
    }
    
    public void setPredicates(List<RoutePredicate> predicates)
    {
        if (predicates == null || predicates.isEmpty())
        {
            this.predicates = null;
        }
        else
        {
            Collections.sort(predicates);
            this.predicates = predicates.toArray(new RoutePredicate[predicates.size()]);
        }
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
    
    public int computeOrder()
    {
        return (this.isRegex() ? 5000 : 0) + (this.isExceptionHandler() ? 10000 : 0) + this.order;
    }

    @Override
    public int compareTo(Route o)
    {
        return Integer.compare(this.computeOrder(), o.computeOrder());
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
        // is this a filter?
        Filter filter = isFilter(method);
        // build the route
        Route r = rb.build(prefix, router, method, a, filter);
        if (r == null) return null;
        // annotations across all routes
        // is it an error route
        List<Class<? extends Throwable>> exceptions = new LinkedList<Class<? extends Throwable>>(); 
        for (Catch error : method.getAnnotationsByType(Catch.class))
        {
            if (error != null)
            {
                for (Class<? extends Throwable> errorClass : error.value())
                {
                    exceptions.add(errorClass);
                }
            }
        }
        r.exceptions(exceptions);
        // is there an order
        Order order = method.getAnnotation(Order.class);
        if (order != null) r.setOrder(order.value());
        // compile the pattern
        r.setCompiledPattern(compilePattern(r.getPrefix(), r.getPattern(), r.isRegex(), r.getAs()));
        // build the predicates
        List<Annotation> predicateAnnotations = getRoutePredicateAnnotations(method);
        if (! predicateAnnotations.isEmpty())
        {
            List<RoutePredicate> predicates = new LinkedList<RoutePredicate>();
            for (Annotation predicateAnnotation : predicateAnnotations)
            {
                RoutePredicateBuilder predicateBuilder = getRoutePredicateBuilder(predicateAnnotation);
                if (predicateBuilder != null)
                {
                    RoutePredicate predicate = predicateBuilder.build(prefix, router, method, predicateAnnotation, filter);
                    if (predicate != null) predicates.add(predicate);
                }
            }
            r.setPredicates(predicates);
        }
        return r;
    }
    
    public static Filter isFilter(Method m)
    {
        if (m.getAnnotation(Before.class) != null) return Filter.BEFORE;
        if (m.getAnnotation(After.class) != null)  return Filter.AFTER;
        return null;
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
        
        public Pattern getPattern()
        {
            return this.pattern;
        }
        
        public String[] getAs()
        {
            return this.as;
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
        Route build(String prefix, Router<?> router, Method method, Annotation a, Filter filter);
    }

    public static class AnyRouteBuilder implements RouteBuilder
    {
        @Override
        public Route build(String prefix, Router<?> router, Method method, Annotation a, Filter filter)
        {
            Any url = (Any) a;
            return new Route(prefix, "ANY", url.value(), url.regex(), url.as(), method, router, filter);
        }
    }

    public static class GetRouteBuilder implements RouteBuilder
    {
        @Override
        public Route build(String prefix, Router<?> router, Method method, Annotation a, Filter filter)
        {
            Get url = (Get) a;
            return new Route(prefix, "GET", url.value(), url.regex(), url.as(), method, router, filter);
        }
    }

    public static class PostRouteBuilder implements RouteBuilder
    {
        @Override
        public Route build(String prefix, Router<?> router, Method method, Annotation a, Filter filter)
        {
            Post url = (Post) a;
            return new Route(prefix, "POST", url.value(), url.regex(), url.as(), method, router, filter);
        }
    }
    
    public static class OptionsRouteBuilder implements RouteBuilder
    {
        @Override
        public Route build(String prefix, Router<?> router, Method method, Annotation a, Filter filter)
        {
            Options url = (Options) a;
            return new Route(prefix, "OPTIONS", url.value(), url.regex(), url.as(), method, router, filter);
        }
    }

    public static class PutRouteBuilder implements RouteBuilder
    {
        @Override
        public Route build(String prefix, Router<?> router, Method method, Annotation a, Filter filter)
        {
            Put url = (Put) a;
            return new Route(prefix, "Put", url.value(), url.regex(), url.as(), method, router, filter);
        }
    }

    public static class DeleteRouteBuilder implements RouteBuilder
    {
        @Override
        public Route build(String prefix, Router<?> router, Method method, Annotation a, Filter filter)
        {
            Delete url = (Delete) a;
            return new Route(prefix, "DELETE", url.value(), url.regex(), url.as(), method, router, filter);
        }
    }
    
    /**
     * A precondition which will be applied to 
     * assert that the given request is applicable 
     * to a route.
     * 
     * Route predicates enable routes to accept or reject 
     * specific requests because they do not match 
     * a specific conditions.
     * 
     */
    public static abstract class RoutePredicate implements Comparable<RoutePredicate>
    {   
        public enum PredicateAction {
            ACCEPT,
            REJECT,
            NEXT
        }
        
        protected final int order;
        
        protected RoutePredicate(final int order)
        {
            super();
            this.order = order;
        }
        
        public int getOrder()
        {
            return this.order;
        }
        
        @Override
        public int compareTo(RoutePredicate o)
        {
            return Integer.compare(this.order, o.order);
        }

        public abstract PredicateAction apply(BalsaContext context, BalsaRequest request);
    }
    
    /**
     * Build a route predicate given an annotated method
     */
    public static interface RoutePredicateBuilder
    {
        RoutePredicate build(String prefix, Router<?> router, Method method, Annotation predicateAnnotation, Filter filter);
    }
    
    public static List<Annotation> getRoutePredicateAnnotations(Method method)
    {
        List<Annotation> builders = new LinkedList<Annotation>();
        for (Annotation a : method.getAnnotations())
        {
            if (a.annotationType().isAnnotationPresent(IsRoutePredicate.class)) builders.add(a);
        }
        return builders;
    }

    public static RoutePredicateBuilder getRoutePredicateBuilder(Annotation a)
    {
        IsRoutePredicate irp = a.annotationType().getAnnotation(IsRoutePredicate.class);
        if (irp == null) return null;
        try
        {
            return irp.value().newInstance();
        }
        catch (Exception e)
        {
            // eat
        }
        return null;
    }
}
