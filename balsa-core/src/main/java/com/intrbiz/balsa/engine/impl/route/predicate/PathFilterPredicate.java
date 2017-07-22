package com.intrbiz.balsa.engine.impl.route.predicate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.regex.Matcher;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.engine.impl.route.Route;
import com.intrbiz.balsa.engine.impl.route.Route.CompiledPattern;
import com.intrbiz.balsa.engine.impl.route.Route.Filter;
import com.intrbiz.balsa.engine.impl.route.Route.RoutePredicate;
import com.intrbiz.balsa.engine.impl.route.Route.RoutePredicateBuilder;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.listener.BalsaRequest;
import com.intrbiz.metadata.PathFilter;

public class PathFilterPredicate extends RoutePredicate
{
    private final CompiledPattern pattern;
    
    private final PredicateAction action;
    
    public PathFilterPredicate(int order, CompiledPattern pattern, PredicateAction action)
    {
        super(order);
        this.pattern = pattern;
        this.action = action;
    }
    
    @Override
    public PredicateAction apply(BalsaContext context, BalsaRequest request)
    {
        Matcher m = this.pattern.pattern.matcher(request.getPathInfo());
        if (m.matches())
        {
            return this.action;
        }
        return PredicateAction.NEXT;
    }

    public static class Builder implements RoutePredicateBuilder
    {
        @Override
        public RoutePredicate build(String prefix, Router<?> router, Method method, Annotation predicateAnnotation, Filter filter)
        {
            PathFilter pfa = (PathFilter) predicateAnnotation;
            CompiledPattern pattern = Route.compilePattern(prefix, pfa.path(), pfa.regex(), null);
            return new PathFilterPredicate(pfa.order(), pattern, pfa.action());
        }
    }
}
