package com.intrbiz.balsa.engine.impl.route.predicate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.engine.impl.route.Route.Filter;
import com.intrbiz.balsa.engine.impl.route.Route.RoutePredicate;
import com.intrbiz.balsa.engine.impl.route.Route.RoutePredicateBuilder;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.BalsaHTTPError;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.balsa.listener.BalsaRequest;
import com.intrbiz.metadata.IgnoreCatch;

public class IgnoreCatchPredicate extends RoutePredicate
{
    private final Class<? extends Throwable>[] errors;
    
    private final HTTPStatus[] statuses;
    
    public IgnoreCatchPredicate(int order, Class<? extends Throwable>[] errors, HTTPStatus[] statuses)
    {
        super(order);
        this.errors = errors;
        this.statuses = statuses;
    }
    
    @Override
    public PredicateAction apply(BalsaContext context, BalsaRequest request)
    {
        Throwable t = context.getException();
        if (t != null)
        {
            for (Class<? extends Throwable> error : this.errors)
            {
                if (error.isInstance(t))
                    return PredicateAction.REJECT;
            }
            if (t instanceof BalsaHTTPError)
            {
                HTTPStatus errorStatus = ((BalsaHTTPError) t).getStatus();
                for (HTTPStatus status : this.statuses)
                {
                    if (status == errorStatus)
                        return PredicateAction.REJECT;
                }
            }
        }
        return PredicateAction.NEXT;
    }

    public static class Builder implements RoutePredicateBuilder
    {
        @Override
        public RoutePredicate build(String prefix, Router<?> router, Method method, Annotation predicateAnnotation, Filter filter)
        {
            IgnoreCatch ic = (IgnoreCatch) predicateAnnotation;
            return new IgnoreCatchPredicate(ic.order(), ic.value(), ic.httpErrors());
        }
    }
}
