package com.intrbiz.balsa.engine.impl.route.predicate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.intrbiz.balsa.engine.impl.route.Route.Filter;
import com.intrbiz.balsa.engine.impl.route.Route.RoutePredicate;
import com.intrbiz.balsa.engine.impl.route.Route.RoutePredicateBuilder;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.listener.BalsaRequest;
import com.intrbiz.metadata.IgnoreMethods;

public class IgnoreMethodsPredicate extends RoutePredicate
{
    private final String[] methods;
    
    public IgnoreMethodsPredicate(int order, String[] methods)
    {
        super(order);
        this.methods = methods;
    }
    
    @Override
    public PredicateAction apply(BalsaRequest request)
    {
        for (String method : this.methods)
        {
            if (method.equals(request.getRequestMethod()))
                return PredicateAction.REJECT;
        }
        return PredicateAction.NEXT;
    }

    public static class Builder implements RoutePredicateBuilder
    {
        @Override
        public RoutePredicate build(String prefix, Router<?> router, Method method, Annotation predicateAnnotation, Filter filter)
        {
            IgnoreMethods im = (IgnoreMethods) predicateAnnotation;
            return new IgnoreMethodsPredicate(im.order(), im.value());
        }
    }
}
