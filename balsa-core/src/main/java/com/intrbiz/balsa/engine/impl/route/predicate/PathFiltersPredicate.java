package com.intrbiz.balsa.engine.impl.route.predicate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import com.intrbiz.balsa.engine.impl.route.Route.Filter;
import com.intrbiz.balsa.engine.impl.route.Route.RoutePredicate;
import com.intrbiz.balsa.engine.impl.route.Route.RoutePredicateBuilder;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.listener.BalsaRequest;
import com.intrbiz.metadata.PathFilter;
import com.intrbiz.metadata.PathFilters;

public class PathFiltersPredicate extends RoutePredicate
{
    private final PathFilterPredicate[] predicates;
    
    private final PredicateAction defaultAction;
    
    public PathFiltersPredicate(int order, PathFilterPredicate[] predicates, PredicateAction defaultAction)
    {
        super(order);
        this.predicates = predicates;
        this.defaultAction = defaultAction;
    }
    
    @Override
    public PredicateAction apply(BalsaRequest request)
    {
        for (PathFilterPredicate predicate : this.predicates)
        {
            PredicateAction action = predicate.apply(request);
            if (action != PredicateAction.NEXT) return action;
        }
        return this.defaultAction;
    }

    public static class Builder implements RoutePredicateBuilder
    {
        @Override
        public RoutePredicate build(String prefix, Router<?> router, Method method, Annotation predicateAnnotation, Filter filter)
        {
            PathFilters pfsa = (PathFilters) predicateAnnotation;
            List<PathFilterPredicate> predicates = new LinkedList<PathFilterPredicate>();
            for (PathFilter pfa : pfsa.value())
            {
                PathFilterPredicate pf = (PathFilterPredicate) new PathFilterPredicate.Builder().build(prefix, router, method, pfa, filter);
                if (pf != null) predicates.add(pf);
            }
            return new PathFiltersPredicate(pfsa.order(), predicates.toArray(new PathFilterPredicate[predicates.size()]), pfsa.defaultAction());
        }
    }
}
