package com.intrbiz.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intrbiz.balsa.engine.impl.route.Route.RoutePredicateBuilder;

/**
 * An annotation designed to annotate an annotation which defines a route predicate
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface IsRoutePredicate {
    Class<? extends RoutePredicateBuilder> value();
}
