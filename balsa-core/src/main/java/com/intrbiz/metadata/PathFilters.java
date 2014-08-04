package com.intrbiz.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.balsa.engine.impl.route.Route.RoutePredicate.PredicateAction;
import com.intrbiz.balsa.engine.impl.route.predicate.PathFiltersPredicate;

/**
 * Define a path filtering route predicate.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@IsRoutePredicate(PathFiltersPredicate.Builder.class)
public @interface PathFilters {
    PathFilter[] value();
    int order() default 0;
    PredicateAction defaultAction() default PredicateAction.NEXT;
}
