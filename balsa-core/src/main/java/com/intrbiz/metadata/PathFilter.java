package com.intrbiz.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.balsa.engine.impl.route.Route.RoutePredicate.PredicateAction;
import com.intrbiz.balsa.engine.impl.route.predicate.PathFilterPredicate;

/**
 * Define a path filtering route predicate.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(PathFilters.class)
@IsRoutePredicate(PathFilterPredicate.Builder.class)
public @interface PathFilter {
    String path();
    boolean regex() default false;
    PredicateAction action();
    int order() default 0;
}
