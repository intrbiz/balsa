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
import com.intrbiz.metadata.IgnorePaths;

public class IgnorePathsPredicate extends RoutePredicate
{
    private final CompiledPattern[] patterns;
    
    public IgnorePathsPredicate(int order, CompiledPattern[] patterns)
    {
        super(order);
        this.patterns = patterns;
    }
    
    @Override
    public PredicateAction apply(BalsaContext context, BalsaRequest request)
    {
        for (CompiledPattern pattern : this.patterns)
        {
            Matcher m = pattern.pattern.matcher(request.getPathInfo());
            if (m.matches())
            {
                return PredicateAction.REJECT;
            }
        }
        return PredicateAction.NEXT;
    }

    public static class Builder implements RoutePredicateBuilder
    {
        @Override
        public RoutePredicate build(String prefix, Router<?> router, Method method, Annotation predicateAnnotation, Filter filter)
        {
            IgnorePaths ipa = (IgnorePaths) predicateAnnotation;
            CompiledPattern[] patterns = new CompiledPattern[ipa.value().length];
            for (int i = 0; i < ipa.value().length; i++)
            {
                patterns[i] = Route.compilePattern(prefix, ipa.value()[i], ipa.regex(), null);
            }
            return new IgnorePathsPredicate(ipa.order(), patterns);
        }
    }
}
