package net.oneandone.spock.orderextension

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 *
 * After defines that this feature will run after the feature of the annotation.
 * <blockquote>
 * <pre>{@code
 * @After('feature1')
 * def feature2() {}
 * }</pre>
 * </blockquote>
 *
 * Execution order is: {@code feature1 -> feature2}
 *
 * @see Order
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface After {
    String value()
}

